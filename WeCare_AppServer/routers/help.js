const express = require('express')
const User = require('../models/user')
const router = new express.Router()
var admin = require("firebase-admin");


var serviceAccount = require("../resource/wecare-1576080754258-firebase-adminsdk-lz17k-853cde1ea4.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://wecare-1576080754258.firebaseio.com"
});

function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {
  var R = 6371; // Radius of the earth in km
  var dLat = deg2rad(lat2-lat1);  // deg2rad below
  var dLon = deg2rad(lon2-lon1); 
  var a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
    Math.sin(dLon/2) * Math.sin(dLon/2)
    ; 
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
  var d = R * c; // Distance in km
  return d;
}

function deg2rad(deg) {
  return deg * (Math.PI/180)
}

const sendNotification = (registrationToken,payload)=>admin.messaging().sendToDevice(registrationToken, payload)
  .then(function(response) {
    console.log("Successfully sent message:", response);
  })
  .catch(function(error) {
    console.log("Error sending message:", error);
  });

router.post('/help', async (req, res) => {

    const google_id = req.body.google_id;
    const name = req.body.name;
    const user =await User.findOne({google_id});
    console.log(user)
    const latitude = user.latitude;
    const longitude = user.longitude;
    console.log(latitude,longitude);


    User.find({}, async function(err, users) {
        console.log(users);
        users.forEach(async (userOne)=>{
            console.log(userOne.name)

            if(userOne.google_id!=google_id)
            {
              var distance = getDistanceFromLatLonInKm(userOne.latitude,userOne.longitude,latitude,longitude);
              console.log("Distance = "+distance);

              var flag=false;

              user.friends.forEach((user_friend)=>{
                  if(userOne.email===user_friend.email)
                  {
                      return flag=true;
                  }

              })

              if(distance < 10 || flag)
                {

                  var payload = {
                    data: {
                      name: name,
                      latitude,
                      longitude,
                      distance : Math.floor(distance).toString()
                    },
                    notification: {
                      title: name+" need your help",
                      body: Math.floor(distance)+" km away from you",
                      click_action: "OPEN_MAP"
                    }
                  };
                  var registrationToken = userOne.token;
                  await sendNotification(registrationToken,payload);
                  console.log("sent");
                }
            }
        })
    });

    res.status(200).send(user);

})

module.exports = router