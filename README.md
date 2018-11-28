# Rotate Marker and Move Animation on Map like Uber,Ola,Lyft,careem in Android

I am working on a project similar to UBER, Lyft or OLA ie. Map on the home with available moving Cars. I was looking for some kind of Library which can make Cars move and take turn smoothly just like UBER,OLA,careem. For now I am able to move car smoothly from one lat-long to another with the mentioned code. But tricky part is Taking turn and make sure the car face to front when moving to direction.

Enjoy the work and ease your working hours :)

# Explained Logics

# rotate the marker with move direction
use 
```map.clear()```
to clear the map and then create marker so whenever your Location changed old marker will automatically be cleared and new marker is created so it seems like marker is moving with the location

#Setting up of Value animator:Create a value animator by providing the ofFloatValue, setting duration and adding update listener in Handler
```ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
valueAnimator.setDuration(3000);
valueAnimator.setInterpolator(new LinearInterpolator());
valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
  @Override
  public void onAnimationUpdate(ValueAnimator valueAnimator) {
    //CODE
}); 
```
# Rotate marker/car icon in google maps - Android
there is simple method available for marker

```marker.rotation(float value)``` 

Sets the rotation of the marker in degrees clockwise about the marker's anchor point. The axis of rotation is perpendicular to the marker. A rotation of 0 corresponds to the default position of the marker. When the marker is flat on the map, the default position is North aligned and the rotation is such that the marker always remains flat on the map. When the marker is a billboard, the default position is pointing up and the rotation is such that the marker is always facing the camera. The default value is 0.
