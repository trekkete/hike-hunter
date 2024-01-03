window.trekkete = {};

window.trekkete.getLocation = function() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(window.trekkete.showPosition);
    }
}

window.trekkete.showPosition = function(position) {
    window.trekkete.coords = {"lat" : position.coords.latitude, "lon" : position.coords.longitude};
}