#!/bin/bash

# Gunicorn 서버 시작 스크립트
# 동시 요청 처리를 위한 최적화된 설정

# 워커 수: min(CPU 코어 수 * 2 + 1, 15)
CALCULATED_WORKERS=$(($(nproc) * 2 + 1))
if [ "$CALCULATED_WORKERS" -gt 15 ]; then
  WORKERS=15
else
  WORKERS=$CALCULATED_WORKERS
fi

# 스레드 수: 워커당 스레드 수
THREADS=4

# 최대 동시 요청 수 = WORKERS * THREADS
echo "Starting Gunicorn with $WORKERS workers, $THREADS threads per worker"
echo "Max concurrent requests: $((WORKERS * THREADS))"

nohup gunicorn voicetutor.wsgi:application \
    --bind 0.0.0.0:8080 \
    --workers $WORKERS \
    --threads $THREADS \
    --worker-class gthread \
    --preload \
    --timeout 600 \
    --keep-alive 5 \
    --max-requests 1000 \
    --max-requests-jitter 50 \
    --access-logfile - \
    --error-logfile - \
    --log-level info \
    > nohup.out 2>&1 &

echo "Gunicorn started with PID: $!"
echo "Logs are being written to nohup.out"
