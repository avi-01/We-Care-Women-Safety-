const express = require('express')
// http = require('http'),
const app = express()
// server = http.createServer(app)
require('./db/mongoose')
const userRouter = require('./routers/user')
const helpRouter = require('./routers/help')

app.use(express.json())
app.use(userRouter)
app.use(helpRouter)

const port  = process.env.PORT || 3000;



// sendNotification(registrationToken,payload)


// io = require('socket.io').listen(server);

app.get('/', (req, res) => {

    res.send('Chat Server is running')
});


app.listen(port,()=>{

    console.log('Node app is running on port '+port)
    
});

// io.on('connection', (socket) => {

//     console.log('user connected')
    
//     socket.on('join', function(userNickname) {
    
//             console.log(userNickname +" : has joined the chat "  )
    
//             socket.broadcast.emit('userjoinedthechat',userNickname +" : has joined the chat ")

//         });
    
//     socket.on("new message",(message)=>{
//         console.log("message "+message);
//     })

//     socket.on("Location",(mes)=>{
//         console.log("Received: "+mes);
//         socket.emit("sendLocation");
//     })

//     });

    

