Android speech Examples
===========

eclipse/ has the examples in eclipse project format, no longer updated.  Otherwise the examples are for android studio.

<b>speech2text</b> use the button to have it recognize the speech.  It will list the top 5 results and speak the top result.  It also have multilingual abilities.  broken in API 30, I think it the package visibility and I can't find the correct intent for the queries since it's all hidden behind other variables.  Even on an android 11 device, it works if set to API 29, fails on API 30.  No documentation as to why.  So this example is set to API 29 so at least it works.

<b>speech2textdemo</b> use the recognizerIntent to get the speech2text engine and displays the top 5 results.  

<b>speech2textdemo2</b> builds it's own interface, instead of using the recognizerIntent.  still displays the top 5 results.

<b>text2speech</b> is a simple example to do text to speech.   works in API 30 with queries additions.

<b>VoiceActionsDemo</b>  Show how to add voice Actions to your app.  This example uses the create_note intent.

<b>VoiceInteractions</b> is an attempt at VoiceInteractions.  Most of the intents don't support voice, but the camera intent does.  So say Take a selfie  OR take a picture.  Take a selfie conforms use of the front camera, while take a picture will ask which camera to use.  A Note, no picture is actually taken by the app.   This example is be considered as legacy, since android is basically abandoned the voice interactions for Google assistant methods.  When I get a change, I'll replace this example with the "new way" and move this to a legacy directory.


These are example code for University of Wyoming, Cosc 4730 Mobile Programming course.
All examples are for Android.
