const express = require('express')
const User = require('../models/user')
const router = new express.Router()


router.post('/users', async (req, res) => {

    console.log("user")
    const user = new User(req.body)
    const google_id = req.body.google_id;
    console.log("google_id: "+google_id)
    const preuser = await User.findOne({google_id})
    console.log(preuser)

    if(preuser==null)
    {
        try {
            await user.save()
            res.status(201).send({ user })
        } catch (e) {
            console.log(e);
            res.status(400).send(e)
        }
    }
    else
    {
        res.status(201).send(preuser);
    }
})

router.patch('/users/me', async (req, res) => {

    console.log(req.query)
    const google_id = req.query.google_id
    const updates = Object.keys(req.body)
    const allowedUpdates = ['latitude','longitude','token']
    const user = await User.findOne({google_id})
    console.log(user)
    const isValidOperation = updates.every((update) => allowedUpdates.includes(update))

    if (!isValidOperation || user==null) {
        return res.status(400).send({ error: 'Invalid updates!' })
    }

    try {
        updates.forEach((update)=>{
            console.log(update,req.body[update])
            user[update] = req.body[update]}
            )
        await user.save()
        res.send(user)
    } catch (e) {
        res.status(400).send(e)
    }
})

router.post("/users/friend",async ( req,res )=>{
  
    console.log(req.query,req.body)
    const google_id = req.query.google_id
    const user = await User.findOne({google_id})

    const friend_email = req.body.friend_email;
    const friend = await User.findOne({email:friend_email})
    
    // console.log(user,friend)
    if(!user || !friend)
    {
        return res.status(404).send()
    }

    var flag=false;

    user.friends.forEach((user_friend)=>{
        if(user_friend.email===friend.email)
        {
            return flag=true;
        }
    })

    if(flag)
    {
        // console.log("exists")
        return res.status(200).send();
    }
    // console.log("friend "+user.friends)
    user.friends = user.friends.concat({
            email:friend.email,
            name:friend.name
        })
    await user.save();

    // console.log(user);
    return res.status(200).send();
})


router.get("/users/friend", async (req,res) =>{

    console.log(req.query)
    const google_id = req.query.google_id
    const user = await User.findOne({google_id})

    if(!user)
    {
        return res.status(404).send()
    }

    const user_friends = user.friends;
    res.status(200).send({user_friends});

})



module.exports = router