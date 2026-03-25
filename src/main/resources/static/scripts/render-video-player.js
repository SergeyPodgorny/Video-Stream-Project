async function getVideoInfo(videoTitle) {
    try {
        const response = await fetch('/playlist');
        if (!response.ok) {
            throw new Error(`Error fetching playlist: ${response.status}`);
        }
        const playlist = await response.json();
        return playlist.find(video => video.title === videoTitle);
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