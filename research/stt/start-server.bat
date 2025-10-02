@echo off
echo 🎤 STT 서버를 시작합니다...
echo.

cd flask-server
echo 📁 Flask 서버 디렉토리로 이동...
echo.

echo 🔧 가상환경 활성화...
call venv\Scripts\activate.bat
echo.

echo 🚀 Flask 서버 시작...
python app.py

pause
