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
        
        console.log('Fetching videos for folder:', folderPath);
        
        const response = await fetch(`/playlist/folder?path=${encodeURIComponent(folderPath)}`);
        if (!response.ok) {
            throw new Error(`Error fetching playlist: ${response.status}`);
        }
        const videos = await response.json();
        return videos.find(video => video.title === videoFileName);
    } catch (error) {
        console.error('Error fetching video info:', error);
        return null;
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

    let videoContainer = '<video id="my-video" class="video-js" controls preload="auto" width="auto" height="auto" data-setup="{}">';

    videoContainer += '<source src="video/'+currentFileName+'" type="video/mp4" />';
    videoContainer += '    <p class="vjs-no-js">To view this video please enable JavaScript, and consider upgrading to a web browser that <a href="https://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a></p>';
    videoContainer += '</video>';

    const container = document.getElementById('video-container');
    container.innerHTML = videoContainer;

    // Load video info
    getVideoInfo(currentFileName).then(renderVideoInfo);
};

renderVideoContainer();