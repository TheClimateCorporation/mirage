Mirage
=======

Mirage is an image loading library developed by the Android team at The Climate Corporation
for loading, caching, and sync'ing for offline usage of images. Our main consideration
for creating this system was to allow for explicit sync'ing of images for offline use.
Libraries like Picasso didn't fulfill our requirements because as stated "Picasso doesn't
have a disk cache. It delegates to whatever HTTP client"




Download
--------
Until the library becomes available on mavenCentral download it via the climate maven
using gradle

``` groovy
dependencies {
    compile 'com.climate.mirage:mirage:{latest:version:number}'
}
```




Examples
--------
There are several examples in the “samples” folder to check out and run.
The general idea is this

```Java

// Simple Request
Mirage.get(context)
    .load("http://wwww.pathtoyourimage.com/folder/image.jpg")
    .into(myImageView)
    .placeHolder(R.drawable.image_holder)
    .error(R.drawable.ic_error)
    .fade()
    .go();


// Request with Headers (for Climate employees)
Mirage.get(context)
    .load("http://wwww.pathtoyourimage.com/folder/image.jpg")
    .urlFactory(new MirageAuthUrlFactory(headers))
    .into(myImageView)
    .placeHolder(R.drawable.image_holder)
    .error(R.drawable.ic_error)
    .fade()
    .go();


// Request with Headers (non Climate)
HashMap<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer {my_special_token}");
Mirage.get(context)
    .load("http://wwww.pathtoyourimage.com/folder/image.jpg")
    .urlFactory(new SimpleUrlConnectionFactory(headers))
    .into(myImageView)
    .placeHolder(R.drawable.image_holder)
    .error(R.drawable.ic_error)
    .fade()
    .go();

```

If you're using this in a ListView, Mirage will automatically cancel the calling
request for you if use a recycled view.




Run the Tests
--------
To run the tests via command line do:
```
./gradlew clean test
```

Note that this is different than "connectedCheck" as there's not device that needs to
be connected. The unit tests is using Robolectric.


To run the tests inside of AndroidStudio, in the "Build Variants" tab near the bottom left,
in Test Artifact select "Unit Tests". Create a new JUnit configuration (Not gradle and not android
tests)




Coverage Reports
--------
To run coverage reports do:
```
./gradlew clean :library:jacocoTestReport
```



Credits
--------
Pablo Picasso is attributed to saying "good artists borrow, great artists steal."
Mirage shamelessly steals from EssentialsLoader, Picasso, Glide, and the DiskCache
and utils Jake Wharton pulled from the Android framework.



License
--------
Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/
