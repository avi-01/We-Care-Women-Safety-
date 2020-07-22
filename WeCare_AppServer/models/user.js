const mongoose = require('mongoose')

const userSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        trim: true
    },
    email: {
        type: String,
        unique: true,
        required: true,
        trim: true,
        lowercase: true
    },
    google_id: {
        type: String,
        unique: true,
        required: true,
        trim: true
    },
    latitude: {
        type: String,
        default: null
    },
    longitude: {
        type: String,
        default: null
    },
    token: {
        type:String,
        default: null
    },
    friends: [{
        email: {
            type: String,
            required: true
        },
        name: {
            type: String,
            required: true
        }
    }]
})

// userSchema.virtual('tasks', {
//     ref: 'Task',
//     localField: '_id',
//     foreignField: 'owner'
// })


userSchema.statics.findByCredentials = async (google_id) => {
    const user = await User.findOne({ google_id })

    if (!user) {
        throw new Error('Unable to login')
    }

    return user
}


const User = mongoose.model('User', userSchema)

module.exports = User