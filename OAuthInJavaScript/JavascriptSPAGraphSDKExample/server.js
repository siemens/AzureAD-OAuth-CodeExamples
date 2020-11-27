/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
*  See LICENSE in the source repository root for complete license information.
*/

/* Graph calls via Graph Client SDK to access Email
*
*  Authors:
*  OneAD team
*
*  Copyright (c) Siemens 2020 - 2022.  [Changed file app/graph.js]. Licensed under the MIT license.
*  See LICENSE in the source repository root for complete license information.
*/

const express = require('express'); // Web server
const morgan = require('morgan'); // Debug logging
const fs = require('fs'); // Serve static from file system
const argv = require('yargs') // CLI args
    .usage('Usage: $0 -p [PORT]')
    .alias('p', 'port')
    .describe('port', '(Optional) Port Number - default is 30662')
    .strict()
    .argv;

const DEFAULT_PORT = 30662;
const APP_DIR = __dirname + `/app`;

//initialize express.
const app = express();

// Initialize variables.
let port = DEFAULT_PORT; // -p {PORT} || 30662;
if (argv.p) {
    port = argv.p;
}

// Configure morgan module to log all requests to console.
app.use(morgan('dev'));

// Serve the frontend.
app.use(express.static('app/'));

// Start the server.
app.listen(port);
console.log(`Listening on port ${port}...`);
