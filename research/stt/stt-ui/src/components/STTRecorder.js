import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import './STTRecorder.css';

const STTRecorder = () => {
  const [isRecording, setIsRecording] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [recordingTime, setRecordingTime] = useState(0);
  const [processingTime, setProcessingTime] = useState(0);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState('');
  // Google STT 전용 (모드 구분 제거)
  // Google STT만 사용
  const [realTimeResults, setRealTimeResults] = useState([]);
  const [chunkCount, setChunkCount] = useState(0);
  const chunkCounterRef = useRef(0);

  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const timerRef = useRef(null);
  const startTimeRef = useRef(null);
  const chunkIntervalRef = useRef(null);
  const currentChunkRef = useRef([]);

  // 녹음 시간 타이머
  useEffect(() => {
    if (isRecording) {
      timerRef.current = setInterval(() => {
        setRecordingTime(prev => prev + 1);
      }, 1000);
    } else {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    }

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    };
  }, [isRecording]);


  // 녹음 시작
  const startRecording = async () => {
    try {
      setError('');
      setTranscript('');
      setRecordingTime(0);
      setProcessingTime(0);
      setUploadProgress(0);
      setRealTimeResults([]);
      setChunkCount(0);
      chunkCounterRef.current = 0; // 카운터 초기화
      audioChunksRef.current = [];
      currentChunkRef.current = [];

      const stream = await navigator.mediaDevices.getUserMedia({ 
        audio: {
          sampleRate: 16000,    // Google STT 표준
          channelCount: 1,      // 모노
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: false  // 압축률 개선
        } 
      });

      // 브라우저별 최적화된 MIME 타입 선택
      let mimeType = 'audio/webm;codecs=opus';
      if (!MediaRecorder.isTypeSupported(mimeType)) {
        mimeType = 'audio/webm';
        if (!MediaRecorder.isTypeSupported(mimeType)) {
          mimeType = 'audio/mp4';
        }
      }

      const mediaRecorder = new MediaRecorder(stream, {
        mimeType: mimeType
      });
      
      console.log(`사용 중인 오디오 포맷: ${mimeType}`);

      mediaRecorderRef.current = mediaRecorder;

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
          currentChunkRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = () => {
        // Google STT로 전체 오디오 처리
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        processWholeAudioWithTimestamps(audioBlob);
        stream.getTracks().forEach(track => track.stop());
      };

      // 실시간 모드일 때 4초마다 청크 처리 (최소 길이 보장)
      // 실시간 모드에서도 전체 녹음을 저장 (청킹하지 않음)

      mediaRecorder.start(1000); // 1초마다 데이터 수집
      setIsRecording(true);
      startTimeRef.current = Date.now();

    } catch (err) {
      setError('마이크 접근 권한이 필요합니다. 브라우저 설정을 확인해주세요.');
      console.error('녹음 시작 오류:', err);
    }
  };

  // 녹음 중지
  const stopRecording = () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      
      // 실시간 모드에서 청크 인터벌 정리
      if (chunkIntervalRef.current) {
        clearInterval(chunkIntervalRef.current);
        chunkIntervalRef.current = null;
      }
    }
  };

  // 오디오 처리 및 전송
  // 전체 오디오를 Google STT로 단어별 타임스탬프와 함께 처리
  const processWholeAudioWithTimestamps = async (audioBlob) => {
    try {
      setIsProcessing(true);
      setError('');
      
      console.log(`전체 오디오 처리 시작: ${audioBlob.size} bytes`);
      
      const formData = new FormData();
      const timestamp = Date.now();
      formData.append('audio', audioBlob, `whole_audio_${timestamp}.webm`);

      const response = await axios.post('http://localhost:5000/api/google-speech', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 30000, // 전체 오디오이므로 타임아웃 증가
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          setUploadProgress(percentCompleted);
        }
      });

      console.log('Google STT 전체 응답:', response.data);

      if (response.data.success && response.data.results) {
        // Google STT 결과 처리 (단어별 타임스탬프)
        const allWords = [];
        let fullText = '';
        
        response.data.results.forEach(result => {
          fullText += result.text + ' ';
          allWords.push(...result.words);
        });
        
        const result = {
          id: 0,
          text: fullText.trim(),
          timestamp: new Date().toLocaleTimeString(),
          processingTime: response.data.processing_time,
          confidence: response.data.results[0]?.confidence || 0.95,
          mode: 'google-stt-whole',
          words: allWords,
          totalWords: response.data.total_words
        };

        console.log('전체 오디오 결과:', result);
        setRealTimeResults([result]); // 하나의 결과로 표시
        setTranscript(fullText.trim());
        setProcessingTime(response.data.processing_time);
      } else {
        setError('음성 인식에 실패했습니다.');
      }
    } catch (err) {
      console.error('전체 오디오 처리 오류:', err);
      setError(`음성 인식 중 오류가 발생했습니다: ${err.message}`);
    } finally {
      setIsProcessing(false);
      setUploadProgress(0);
    }
  };


  // 시간 포맷팅
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="stt-recorder">
      <div className="header">
        <h1>🎯 Google STT 음성 인식</h1>
        <p>단어별 타임스탬프가 포함된 정확한 음성 인식 서비스</p>
        

      </div>

      <div className="recording-section">
        <div className="recording-controls">
          {!isRecording ? (
            <button 
              className="record-btn start-btn"
              onClick={startRecording}
              disabled={isProcessing}
            >
              🎤 녹음 시작
            </button>
          ) : (
            <button 
              className="record-btn stop-btn"
              onClick={stopRecording}
            >
              ⏹️ 녹음 중지
            </button>
          )}
        </div>

        {isRecording && (
          <div className="recording-indicator">
            <div className="pulse"></div>
            <span>녹음 중... {formatTime(recordingTime)}</span>
          </div>
        )}

        {isProcessing && (
          <div className="processing-section">
            <div className="progress-bar">
              <div 
                className="progress-fill" 
                style={{ width: `${uploadProgress}%` }}
              ></div>
            </div>
            <p>음성을 텍스트로 변환 중... {uploadProgress}%</p>
          </div>
        )}
      </div>

      {error && (
        <div className="error-message">
          ❌ {error}
        </div>
      )}

      {/* Google STT 결과 표시 */}
        {realTimeResults.length > 0 && (
        <div className="realtime-section">
          <h3>🎯 Google STT 인식 결과</h3>
          <div className="realtime-results">
            {realTimeResults.map((result) => (
              <div key={result.id} className="realtime-item">
                <div className="realtime-header">
                  <div className="realtime-timestamp">{result.timestamp}</div>
                  <div className="realtime-confidence">
                    신뢰도: {(result.confidence * 100).toFixed(1)}%
                  </div>
                </div>
                <div className="realtime-text">{result.text}</div>
                <div className="realtime-processing">
                  처리시간: {result.processingTime}초 | 모드: {result.mode}
                  {result.totalWords && ` | 단어 수: ${result.totalWords}`}
                </div>
                {result.words && result.words.length > 0 && (
                  <div className="word-timestamps">
                    <div className="word-timestamps-header">
                      <h4>🎯 단어별 타임스탬프</h4>
                      <span className="word-count">총 {result.words.length}개 단어</span>
                    </div>
                    <div className="words-list">
                      {result.words.map((word, index) => (
                        <div key={index} className="word-item">
                          <div className="word-time-badge">
                            {word.start_time.toFixed(2)}s
                          </div>
                          <div className="word-content">
                            <span className="word-text">"{word.word}"</span>
                            <div className="word-details">
                              <span className="word-duration">
                                {word.end_time - word.start_time > 0 
                                  ? `${(word.end_time - word.start_time).toFixed(2)}초`
                                  : '0.01초'
                                }
                              </span>
                              <span className="word-confidence">
                                신뢰도: {(word.confidence * 100).toFixed(1)}%
                              </span>
                            </div>
                          </div>
                          <div className="word-confidence-bar">
                            <div 
                              className="confidence-fill"
                              style={{ 
                                width: `${word.confidence * 100}%`,
                                backgroundColor: word.confidence > 0.9 ? '#28a745' : 
                                               word.confidence > 0.7 ? '#ffc107' : '#dc3545'
                              }}
                            ></div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
        )}

      <div className="instructions">
        <h3>📋 사용 방법</h3>
        <ol>
          <li>🎤 <strong>녹음 시작</strong> 버튼을 클릭하세요</li>
          <li>🗣️ 마이크에 대고 명확하게 말씀하세요</li>
          <li>⏹️ <strong>녹음 중지</strong> 버튼을 클릭하세요</li>
          <li>🎯 <strong>Google STT</strong>로 단어별 타임스탬프와 함께 변환됩니다</li>
          <li>📊 각 단어의 정확한 시작/종료 시간을 확인할 수 있습니다</li>
        </ol>
      </div>
    </div>
  );
};

export default STTRecorder;
