async function getVideoInfo(videoPath) {
    try {
        // Извлекаем путь к папке из полного пути к видео
        const lastSlashIndex = videoPath.lastIndexOf('/');
        let folderPath = '.';
        let videoFileName = videoPath;
        
        if (lastSlashIndex !== -1) {
            folderPath = videoPath.substring(0, lastSlashIndex);
            videoFileName = videoPath.substring(lastSlashIndex + 1);
        }
        
        console.log('Fetching videos for folder:', folderPath, 'file:', videoFileName);
        
        const response = await fetch(`/playlist/folder?path=${encodeURIComponent(folderPath)}`);
        if (!response.ok) {
            console.warn('Folder playlist request failed, inferring from file extension');
            throw new Error(`Error fetching playlist: ${response.status}`);
        }
        const videos = await response.json();
        console.log('Videos in folder:', videos);
        let video = videos.find(v => v.title === videoFileName);
        
        // Если видео не найдено в списке, создаём информацию на основе расширения
        if (!video) {
            console.warn('Video not found in playlist, inferring from file extension');
            const ext = videoFileName.split('.').pop().toLowerCase();
            const nativeFormats = ['mp4', 'webm', 'ogg'];
            const requiresTranscoding = !nativeFormats.includes(ext);
            
            video = {
                title: videoFileName,
                format: ext.toUpperCase(),
                requiresTranscoding: requiresTranscoding,
                url: '/streamer_page.html?video=' + encodeURIComponent(videoPath)
            };
        }
        
        // Проверяем, требует ли видео транскодирования
        if (video && video.requiresTranscoding) {
            // Для видео с транскодированием меняем URL
            video.transcodedUrl = `/video/transcoded/${encodeURIComponent(videoPath)}`;
            console.log('Video requires transcoding, using URL:', video.transcodedUrl);
        } else {
            console.log('Video is native format, using URL: video/' + encodeURIComponent(videoPath));
        }
        
        return video;
    } catch (error) {
        console.error('Error fetching video info:', error);
        // Возвращаем информацию на основе расширения файла
        const videoFileName = videoPath.substring(videoPath.lastIndexOf('/') + 1);
        const ext = videoFileName.split('.').pop().toLowerCase();
        const nativeFormats = ['mp4', 'webm', 'ogg'];
        const requiresTranscoding = !nativeFormats.includes(ext);
        
        console.log('Fallback: requiresTranscoding =', requiresTranscoding);
        
        return {
            title: videoFileName,
            format: ext.toUpperCase(),
            requiresTranscoding: requiresTranscoding,
            transcodedUrl: requiresTranscoding ? `/video/transcoded/${encodeURIComponent(videoPath)}` : null
        };
    }
}

function renderVideoInfo(videoInfo) {
    if (!videoInfo) return;
    
    const infoContainer = document.getElementById('video-info');
    infoContainer.innerHTML = `
        <strong>Файл:</strong> ${videoInfo.title}<br>
        <strong>Формат:</strong> ${videoInfo.format}
        ${videoInfo.requiresTranscoding ? ' (транскодировка в MP4)' : ''}
    `;
    
    if (videoInfo.requiresTranscoding) {
        document.getElementById('transcoding-warning').style.display = 'block';
    }
}

function renderVideoContainer() {
    const params = new URL(document.location).searchParams;
    const currentFileName = params.get("video");

    console.log('Playing:', currentFileName);

    // Сначала получаем информацию о видео, чтобы определить, нужно ли транскодирование
    getVideoInfo(currentFileName).then(videoInfo => {
        renderVideoInfo(videoInfo);
        
        let videoUrl;
        if (videoInfo && videoInfo.requiresTranscoding && videoInfo.transcodedUrl) {
            // Используем URL для транскодированного видео
            videoUrl = videoInfo.transcodedUrl;
            console.log('Using transcoded URL:', videoUrl);
        } else {
            // Используем обычный URL
            videoUrl = 'video/' + currentFileName;
            console.log('Using native URL:', videoUrl);
        }

        let videoContainer = '<video id="my-video" class="video-js" controls preload="auto" width="auto" height="auto" data-setup="{}">';
        videoContainer += '<source src="' + videoUrl + '" type="video/mp4" />';
        videoContainer += '<p class="vjs-no-js">To view this video please enable JavaScript, and consider upgrading to a web browser that <a href="https://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a></p>';
        videoContainer += '</video>';

        const container = document.getElementById('video-container');
        container.innerHTML = videoContainer;
    });
};

renderVideoContainer();