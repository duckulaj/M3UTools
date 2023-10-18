$(document).ready(function() {
    var video = document.getElementById('videoPlayer');

    // Add event listener for seek
    video.addEventListener('seeking', function() {
        console.log('Seeking to: ' + video.currentTime);
    });
});
