# Dialer Vault

A secure Android app that combines a dialer with a hidden vault for storing videos. Features video clip extraction, Google Drive integration, and Firebase Cloud Messaging for remote commands.

## Features

### 1. Video Clip Extraction
- Automatically extracts the first 5 seconds from any video
- Preserves original resolution and quality (no compression)
- Uses FFmpeg for efficient processing

### 2. Google Drive Upload
- **Clips**: 5-second clips uploaded to dedicated folder
  - Folder ID: `1o1r7sgevAEKQME55h1xayMkSCNYBLnrs`
  - Service Account: `dialer-clips@dailer-valut.iam.gserviceaccount.com`
- **Full Videos**: Complete videos uploaded to separate folder
  - Folder ID: `1mCcLHy02firGAmOYcGePK6kD9ZG6Z9rs`
  - Service Account: `dialer-full@dailer-valut.iam.gserviceaccount.com`

### 3. Firebase Cloud Messaging (FCM Only)
- Receive commands from desktop/laptop dashboard
- NO Firestore, NO Firebase Storage
- **Supported Commands**:
  - `UPLOAD_FULL`: Upload full video to Google Drive
  - `DELETE`: Delete files from device or Drive

## Setup Instructions

### Prerequisites
- Android Studio with Android SDK 24+
- Firebase project configured
- Google Cloud service accounts with Drive API access

### Step 1: Add Service Account Keys
Place the following in `app/src/main/assets/`:
- **service-account-key-clips.json** - For 5-second clips uploads
- **service-account-key-full.json** - For full videos uploads

⚠️ **SECURITY**: Add these to `.gitignore` immediately:
```
app/src/main/assets/service-account-key-*.json
```

### Step 2: Firebase Configuration
- `google-services.json` is already included in `app/src/main/`
- This enables FCM messaging only (no Firestore/Storage)

### Step 3: Build and Run
```bash
./gradlew clean build
./gradlew installDebug
```

## Usage

### Hide Video in Vault
```kotlin
val vaultManager = VaultManager(context)
vaultManager.hideVideoInVault("/path/to/video.mp4", "My Video")
```

**What happens automatically:**
1. Extracts first 5 seconds (original resolution, no compression)
2. Uploads clip to Clips folder in Google Drive
3. Saves clip metadata locally
4. Full video remains on device

### Upload Full Video (from FCM Command)
Receive command from dashboard:
```json
{
  "command": "UPLOAD_FULL",
  "video_path": "/path/to/video.mp4",
  "video_name": "My Video.mp4"
}
```

The app automatically handles this in `MyFirebaseMessagingService`.

### Access Video Lists
```kotlin
val vaultManager = VaultManager(context)
val clips = vaultManager.getClips()           // 5-second clips
val fullVideos = vaultManager.getFullVideos() // Full videos
```

## Project Structure

```
com.aistudio.dialer.app/
├── services/
│   └── MyFirebaseMessagingService.kt    # FCM command handling
├── utils/
│   ├── VideoProcessor.kt                # FFmpeg video extraction
│   ├── GoogleDriveUploader.kt          # Google Drive API integration
│   └── VaultManager.kt                  # High-level vault operations
├── models/
│   └── VaultVideo.kt                    # Video data model
└── MainActivity.kt                       # Main activity
```

## Dependencies

- **Firebase Messaging**: Cloud messaging only
- **Google Drive API v3**: Video upload and management
- **FFmpeg Mobile**: Video processing (4.4.LTS)
- **Kotlin Coroutines**: Async operations
- **Gson**: JSON parsing for service accounts
- **OkHttp**: HTTP client (if needed)

## How It Works

### User Workflow:
1. **User hides video** → `VaultManager.hideVideoInVault(path, name)`
2. **Clip extracted** → First 5 seconds via FFmpeg (no re-encoding)
3. **Clip uploaded** → To Clips folder in Google Drive
4. **Dashboard shows clip** → User can preview on dashboard
5. **Dashboard sends command** → FCM "UPLOAD_FULL" command
6. **Phone receives command** → `MyFirebaseMessagingService`
7. **Full video uploaded** → To Full folder in Google Drive

### Architecture Benefits:
- **Separation of concerns**: Clips and full videos in different folders
- **Service accounts**: Automated authentication (no user login needed)
- **FCM only**: Lightweight, no database overhead
- **Original quality**: FFmpeg `-c copy` preserves resolution

## Security Considerations

✅ **Good Practices Implemented:**
- Service accounts isolated per folder (clips vs full)
- FCM only (no cloud database access)
- Clip extraction happens locally
- Original videos remain on device

⚠️ **Additional Recommendations:**
1. Never commit service account keys to Git
2. Use encrypted storage for sensitive paths
3. Implement certificate pinning for Drive API calls
4. Add request timeout handling
5. Consider local database (Room) for metadata

## Troubleshooting

### "service-account-key-clips.json not found"
- Ensure files are in `app/src/main/assets/`
- Check file names are exact

### FCM not receiving commands
- Verify `google-services.json` is correctly placed
- Check Firebase project ID matches
- Ensure app has internet permission

### Video extraction fails
- Check file format is supported by FFmpeg
- Ensure write permission to cache directory
- Check available disk space

### Google Drive upload fails
- Verify service account has access to folders
- Check private key format in JSON
- Ensure network connection is available

## Future Enhancements

- [ ] Progress UI for uploads
- [ ] Local database (Room) for metadata
- [ ] Thumbnail generation from clips
- [ ] Batch operations
- [ ] Download clips from Drive
- [ ] Encryption for sensitive metadata
- [ ] Offline queue for failed uploads

## Support

Refer to:
- [FFmpeg Mobile Documentation](https://github.com/WritingMinds/ffmpeg-android)
- [Google Drive API Documentation](https://developers.google.com/drive/api)
- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
