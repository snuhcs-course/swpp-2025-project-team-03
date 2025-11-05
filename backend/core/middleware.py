"""
Exception logging middleware for detailed error logging.
This middleware automatically logs all exceptions with full traceback to stderr (nohup.out).
"""

import logging
import sys
import traceback

logger = logging.getLogger("django.request")


class ExceptionLoggingMiddleware:
    """
    Middleware to automatically log all exceptions with full traceback.
    This ensures that all 500 errors are logged with detailed traceback to nohup.out
    without requiring manual logger calls in views.

    Handles two cases:
    1. Unhandled exceptions: process_exception() is called
    2. Handled exceptions (500 responses): detected in __call__()
    """

    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        # Store request for exception logging
        self.request = request

        try:
            response = self.get_response(request)

            # Check if response is a 500 error (even if exception was caught in views)
            if hasattr(response, "status_code") and response.status_code == 500:
                # Log 500 error even if exception was caught in views
                # Note: traceback may not be available if exception was caught
                self._log_500_error(request, response)

            return response
        except Exception as e:
            # This shouldn't normally happen, but just in case
            self._log_exception(request, e)
            raise

    def process_exception(self, request, exception):
        """
        Process any unhandled exception that occurs during request handling.
        Logs the full traceback with request details.
        """
        self._log_exception(request, exception)

        # Return None to let Django handle the exception normally
        return None

    def _log_exception(self, request, exception):
        """Log exception with full traceback"""
        # Get full traceback
        exc_type, exc_value, exc_traceback = sys.exc_info()
        if exc_traceback:
            tb_lines = traceback.format_exception(exc_type, exc_value, exc_traceback)
            full_traceback = "".join(tb_lines)
        else:
            full_traceback = f"{type(exception).__name__}: {str(exception)}"

        # Log with detailed information
        error_message = (
            f"\n{'=' * 80}\n"
            f"UNHANDLED EXCEPTION OCCURRED\n"
            f"{'=' * 80}\n"
            f"Path: {request.path}\n"
            f"Method: {request.method}\n"
            f"User: {getattr(request, 'user', 'Anonymous')}\n"
            f"Exception Type: {type(exception).__name__}\n"
            f"Exception Message: {str(exception)}\n"
            f"{'-' * 80}\n"
            f"Full Traceback:\n"
            f"{full_traceback}\n"
            f"{'=' * 80}\n"
        )

        # Log to stderr (which goes to nohup.out)
        logger.error(error_message, exc_info=True)

        # Also print to stderr directly to ensure it's captured
        print(error_message, file=sys.stderr, flush=True)

    def _log_500_error(self, request, response):
        """Log 500 error response (even if exception was caught in views)"""
        # Try to get exception info if available
        exc_type, exc_value, exc_traceback = sys.exc_info()

        error_message = (
            f"\n{'=' * 80}\n"
            f"500 INTERNAL SERVER ERROR\n"
            f"{'=' * 80}\n"
            f"Path: {request.path}\n"
            f"Method: {request.method}\n"
            f"User: {getattr(request, 'user', 'Anonymous')}\n"
        )

        if exc_type and exc_value:
            # If exception info is still available, include it
            tb_lines = traceback.format_exception(exc_type, exc_value, exc_traceback)
            full_traceback = "".join(tb_lines)
            error_message += (
                f"Exception Type: {exc_type.__name__}\n"
                f"Exception Message: {str(exc_value)}\n"
                f"{'-' * 80}\n"
                f"Full Traceback:\n"
                f"{full_traceback}\n"
            )
        else:
            # Exception was caught in views, traceback may not be available
            error_message += "Note: Exception was caught in views. Check view code for error handling.\n"
            # Try to get error details from response if available
            if hasattr(response, "data") and isinstance(response.data, dict):
                error_detail = response.data.get("error") or response.data.get("detail")
                if error_detail:
                    error_message += f"Error from response: {error_detail}\n"

        error_message += f"{'=' * 80}\n"

        # Log to stderr (which goes to nohup.out)
        logger.error(error_message, exc_info=True)

        # Also print to stderr directly to ensure it's captured
        print(error_message, file=sys.stderr, flush=True)
