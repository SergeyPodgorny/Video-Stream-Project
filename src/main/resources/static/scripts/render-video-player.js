function renderVideoContainer() {

    let params = (new URL(document.location)).searchParams;
    let currentFileName = params.get("video");

    console.log(currentFileName);
    console.log('<source src="video/'+currentFileName+'"/>');


    let videoContainer = '<video    id="my-video"    class="video-js"    controls    preload="auto"    width="auto"    height="auto"    data-setup="{}">';

    videoContainer+= '<source src="video/'+currentFileName+'" type="video/mp4" />';
    videoContainer+='    <p class="vjs-no-js">      To view this video please enable JavaScript, and consider upgrading to a      web browser that      <a href="https://videojs.com/html5-video-support/" target="_blank"        >supports HTML5 video</a ></p></video>';

  const container = document.getElementById('video-container');
  container.innerHTML = videoContainer;

  };

  renderVideoContainer();