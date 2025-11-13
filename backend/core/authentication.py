"""
Custom JWT authentication that reads tokens from cookies.
"""

import logging

from rest_framework_simplejwt.authentication import JWTAuthentication
from rest_framework_simplejwt.exceptions import AuthenticationFailed, InvalidToken

logger = logging.getLogger(__name__)


class CookieJWTAuthentication(JWTAuthentication):
    """
    JWT authentication that reads the token from cookies instead of Authorization header.
    Falls back to Authorization header if cookie is not present.
    """

    def authenticate(self, request):
        # Try to get token from cookie first
        access_token = request.COOKIES.get("access_token")

        logger.info(f"[CookieJWTAuthentication] Path: {request.path}, Method: {request.method}")
        logger.info(f"[CookieJWTAuthentication] Cookies: {list(request.COOKIES.keys())}")
        logger.info(f"[CookieJWTAuthentication] access_token present: {bool(access_token)}")

        if access_token:
            # Validate the token from cookie
            try:
                validated_token = self.get_validated_token(access_token)
                user = self.get_user(validated_token)
                logger.info(f"[CookieJWTAuthentication] Authentication successful for user: {user.id}")
                return (user, validated_token)
            except (InvalidToken, AuthenticationFailed) as e:
                logger.warning(f"[CookieJWTAuthentication] Invalid token from cookie: {e}")
                # If cookie token is invalid, try Authorization header as fallback
                pass

        # Check Authorization header
        auth_header = request.META.get("HTTP_AUTHORIZATION")
        logger.info(f"[CookieJWTAuthentication] Authorization header present: {bool(auth_header)}")

        # Fallback to Authorization header (standard JWT authentication)
        result = super().authenticate(request)
        if result:
            logger.info("[CookieJWTAuthentication] Authentication successful via Authorization header")
        else:
            logger.warning("[CookieJWTAuthentication] Authentication failed - no valid token found")
        return result
