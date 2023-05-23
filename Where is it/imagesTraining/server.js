const express = require("express");
const app = express();
const multer = require('multer')
const path = require('path');
const fs = require('fs');
var bodyParser = require('body-parser');
const PORT = process.env.PORT || 3000

var exec = require('child_process').exec;


app.use(express.static(path.join(__dirname, 'public')));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

const pathDarknetLinux = "/home/omar/Downloads/darknet/";
const pathForTrainLinux = pathDarknetLinux + "data/";
const pathForDataNamesLinux = pathDarknetLinux + "custom_cfg/"
const pathForImagesLinux = pathForTrainLinux + "images/"
const pathForlabelsLinux = pathForImagesLinux;

var maxBatches = 1000;


const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, pathForImagesLinux)
    },
    filename: function (req, file, cb) {
        cb(null, file.originalname)
    }
});

const multi_upload = multer({
    storage,
    limits: {
        fileSize: 20 * 1024 * 1024
    }, // 20MB
    fileFilter: (req, file, cb) => {
        if (file.mimetype == "image/png" || file.mimetype == "image/jpg" || file.mimetype == "image/jpeg") {
            cb(null, true);
        } else {
            cb(null, false);
            const err = new Error('Only .png, .jpg and .jpeg format allowed!')
            err.name = 'ExtensionError'
            return cb(err);
        }
    },
}).array('uploadedImages', 20)

app.post('/train', (req, res) => {
    try{
        fs.readFileSync("public/newTraining/yolov3-tiny.cfg")
        fs.rmSync("public/newTraining/yolov3-tiny.cfg")
        const data = fs.readFileSync(pathForDataNamesLinux + "custom.names", 'utf8');
        const classNum = data.split("\n").length
        const filters = (classNum + 5) * 3
        const per2 = maxBatches * 0.9
        const per1 = maxBatches * 0.8

        exec("./trainCommands.sh " + maxBatches + " " + classNum + " " + filters + " " + per1 + " " + per2, function (error, stdout, stderr) {
            console.log('stdout: ' + stdout);
            console.log('stderr: ' + stderr);
            if (error != null) {
                console.error(error)
            } else {
                console.log('exec done');
            }
        });
        res.status(200).end('Your objects are being trained');
    }catch{
        res.status(404).end('Training is already under way');
    }
})

app.post('/uploadImages', (req, res) => {
	var flag = 0;
    multi_upload(req, res, function (err) {
        if (err instanceof multer.MulterError) {
            console.log(err.message)
            // A Multer error occurred when uploading.
            res.status(500).send({
                error: {
                    message: `Multer uploading error: ${err.message}`
                }
            }).end();
            return;
        } else if (err) {
            // An unknown error occurred when uploading.
            if (err.name == 'ExtensionError') {
                res.status(413).send({
                    error: {
                        message: err.message
                    }
                }).end();
            } else {
                res.status(500).send({
                    error: {
                        message: `unknown uploading error: ${err.message}`
                    }
                }).end();
            }
            return;
        }

        // Everything went fine.
        // show file `req.files`
        // show body `req.body`

        var nameLabel = req.body.nameLabel  
        var reqBody = req.body      
        try {
            console.log("inside try")
	if(flag ==0)
	    	fs.appendFileSync(pathForDataNamesLinux + 'custom.names',nameLabel );
	else
		fs.appendFileSync(pathForDataNamesLinux + 'custom.names',"\n" +nameLabel );
            const data1 = fs.readFileSync(pathForDataNamesLinux + "custom.names", 'utf8');
            const myArray = data1.split("\n")
            var classId = 0;
            for (let index = 0; index < myArray.length; index++) {
                if (myArray[index].localeCompare(nameLabel)) {
                    classId = index
                }
            }
            var trainTXT = ""
            for (let index = 0; index < req.files.length; index++) {
                var fileName = req.files[index].originalname
                var trainString = ""
		if(index==0)
                	trainString = pathForImagesLinux + fileName
		else
			trainString = "\n" + pathForImagesLinux + fileName
                trainTXT += trainString
                var width = reqBody[fileName].split("_")[0]
                var height = reqBody[fileName].split("_")[1]
                var centerX = reqBody[fileName].split("_")[2]
                var centerY = reqBody[fileName].split("_")[3]
                var stringToWrite = classId + "\t"+centerX+"\t"+centerY+"\t"+width+"\t" + height
                var name = (req.files[index].originalname).split(".")[0]
                fs.writeFileSync(pathForlabelsLinux + name + ".txt", stringToWrite)
            }
            fs.appendFileSync(pathForTrainLinux + 'train.txt',trainTXT)
            fs.appendFileSync(pathForTrainLinux + 'valid.txt', trainTXT)
            fs.appendFileSync(pathForTrainLinux + 'test.txt', trainTXT)

            var writeFileData = "classes= " + myArray.length + " \ntrain  = "+pathForTrainLinux+"train.txt \nvalid = "+pathForTrainLinux+"valid.txt \n"
                                    + "names ="+pathDarknetLinux+"custom_cfg/custom.names\nbackup = "+pathDarknetLinux+"backup\neval=coco";
            
            fs.writeFileSync(pathForDataNamesLinux + "custom.data", writeFileData)

            maxBatches = myArray.length * 1000

	    var writeCfg = "[net]\n# Testing\n# batch=1\n# subdivisions=1\n# Training\nbatch=64\nsubdivisions=4\nwidth=416\nheight=416\nchannels=3\nmomentum=0.9\ndecay=0.0005\nangle=0\nsaturation = 1.5\nexposure = 1.5\nhue=.1\n\nlearning_rate=0.001\nburn_in=1000\nmax_batches = "+maxBatches+"\npolicy=steps\nsteps="+maxBatches*0.8+","+maxBatches*0.9+"\nscales=.1,.1\n\n\n[convolutional]\nbatch_normalize=1\nfilters=16\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[maxpool]\nsize=2\nstride=2\n\n[convolutional]\nbatch_normalize=1\nfilters=32\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[maxpool]\nsize=2\nstride=2\n\n[convolutional]\nbatch_normalize=1\nfilters=64\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[maxpool]\nsize=2\nstride=2\n\n[convolutional]\nbatch_normalize=1\nfilters=128\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[maxpool]\nsize=2\nstride=2\n\n[convolutional]\nbatch_normalize=1\nfilters=256\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[maxpool]\nsize=2\nstride=2\n\n[convolutional]\nbatch_normalize=1\nfilters=512\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[maxpool]\nsize=2\nstride=1\n\n[convolutional]\nbatch_normalize=1\nfilters=1024\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n###########\n\n[convolutional]\nbatch_normalize=1\nfilters=256\nsize=1\nstride=1\npad=1\nactivation=leaky\n\n[convolutional]\nbatch_normalize=1\nfilters=512\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[convolutional]\nsize=1\nstride=1\npad=1\nfilters="+ (myArray.length+5)*3 +"\nactivation=linear\n\n\n\n[yolo]\nmask = 6,7,8\nanchors = 4,7, 7,15, 13,25,   25,42, 41,67, 75,94,   91,162, 158,205, 250,332\nclasses="+ myArray.length +"\nnum=9\njitter=.3\nignore_thresh = .7\ntruth_thresh = 1\nrandom=1\n\n[route]\nlayers = -4\n\n[convolutional]\nbatch_normalize=1\nfilters=128\nsize=1\nstride=1\npad=1\nactivation=leaky\n\n[upsample]\nstride=2\n\n[route]\nlayers = -1, 8\n\n[convolutional]\nbatch_normalize=1\nfilters=256\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[convolutional]\nsize=1\nstride=1\npad=1\nfilters="+ (myArray.length+5)*3 +"\nactivation=linear\n\n[yolo]\nmask = 3,4,5\nanchors = 4,7, 7,15, 13,25,   25,42, 41,67, 75,94,   91,162, 158,205, 250,332\nclasses="+ myArray.length +"\nnum=9\njitter=.3\nignore_thresh = .7\ntruth_thresh = 1\nrandom=1\n\n\n\n[route]\nlayers = -3\n\n[convolutional]\nbatch_normalize=1\nfilters=128\nsize=1\nstride=1\npad=1\nactivation=leaky\n\n[upsample]\nstride=2\n\n[route]\nlayers = -1, 6\n\n[convolutional]\nbatch_normalize=1\nfilters=128\nsize=3\nstride=1\npad=1\nactivation=leaky\n\n[convolutional]\nsize=1\nstride=1\npad=1\nfilters="+ (myArray.length+5)*3 +"\nactivation=linear\n\n[yolo]\nmask = 0,1,2\nanchors = 4,7, 7,15, 13,25,   25,42, 41,67, 75,94,   91,162, 158,205, 250,332\nclasses="+ myArray.length +"\nnum=9\njitter=.3\nignore_thresh = .7\ntruth_thresh = 1\nrandom=1"
            fs.writeFileSync(pathForDataNamesLinux + "yolov3-tiny.cfg", writeCfg)

            console.log("Done")

        } catch (err) {
            res.status(413).send({
                error: {
                    message: err.message
                }
            }).end();
            console.log(err.message);
            return;
        }
        res.status(200).end('Your files uploaded.');
    })
});


app.listen(PORT, () => {
    console.log(`server started on port ${PORT}`);
});
