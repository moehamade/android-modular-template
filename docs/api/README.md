# API Documentation

This directory contains API contract documentation for Zencastr backend services.

## Purpose

Since the backend APIs are not yet implemented, this documentation serves as:
- **Contract definition** for frontend-backend communication
- **Reference** for backend developers when implementing APIs
- **Mock data guide** for current development
- **OpenAPI spec** when ready for production

## Structure

- `authentication.md` - Auth endpoints (login, register, refresh token)
- `user.md` - User profile management
- `recording.md` - Recording session endpoints
- `websocket.md` - Real-time communication spec

## Status

⚠️ **All APIs are currently mocked**. Real implementation pending.

## Contributing

When the backend is ready:
1. Update these docs with actual endpoints
2. Add OpenAPI/Swagger spec
3. Include example requests/responses
4. Document error codes and handling

