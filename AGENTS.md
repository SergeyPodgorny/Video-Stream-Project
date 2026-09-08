# Video Streamer Project - Agent Guide

## Project Overview
This is a Spring Boot web application that serves local video files through HTTP with support for range requests and transcoding.

## Key Commands
- `mvn spring-boot:run` - Run the application
- `mvn test` - Run all tests
- `mvn compile` - Compile the project

## Architecture
- Uses Spring WebFlux (Reactive) for handling video streaming
- Supports MP4, WebM, and OGV video formats
- Transcoding to WebM for unsupported formats using FFmpeg
- Folder-based video organization with recursive discovery

## Key Files/Endpoints
- `VideoWebProviderController.java` - Main video serving endpoint at `/video/**`
- `VideoWebProviderController.java` - Transcoded video endpoint at `/video/transcoded/**`
- `StreamerApplication.java` - Main application entrypoint
- `FfmpegService.java` - FFmpeg integration for transcoding

## Special Considerations
- Video files are served from static resources
- Range requests are supported for partial content delivery
- FFmpeg is required for transcoding unsupported video formats
- Application config in application.yml (not present, likely in resources directory)
- Uses Spring's reactive programming model with WebFlux

## Context Usage Rules
For complex tasks requiring exploration or major changes:
- When using subagents (explore, general), ensure coordination between agent sessions
- Always verify that subagent outputs are consistent with the overall change plan
- For multi-part tasks, consider using the /opsx-update-change command to incorporate findings into the main change before proceeding
- Result of subagent work must be a summary of 20-30 symbols maximum
- Try always to use subagents
- When summarizing, do not quote code, but keep code references
- Read files by chunks, not whole files