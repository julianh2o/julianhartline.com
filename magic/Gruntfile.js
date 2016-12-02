module.exports = function (grunt) {
    require('load-grunt-tasks')(grunt);

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        browserify: {
            dist: {
                options: {
                    transform: [
                        ["babelify", {
                            loose: "all"
                        }]
                    ]
                },
                files: {
                    "./dist/client/bundle.js": ["./modules/client.js"]
                }
            }
        },
        watch: {
            scripts: {
                files: ["./modules/*.js"],
                tasks: ["browserify"]
            },
            pubfiles: {
                files: ["./public/*"],
                tasks: ["copy"]
            }
        },
        copy: {
            main: {
                expand: true,
                cwd: 'public',
                src: '*',
                dest: 'dist/client/',
            },
        },
        babel: {
            options: {
                sourceMap: true,
                presets: ['es2015']
            },
            dist: {
                expand: true,
                cwd: 'modules',
                src: '*',
                dest: 'dist/server'
            }
        },
        run: {
            options: {
                // Task-specific options go here. 
            },
            server: {
                cmd: 'node',
                args: [
                    'dist/server/server.js',
                ]
            }
        }
    });

    grunt.registerTask("default", ["browserify","babel","copy","run"]);
    grunt.registerTask("build", ["browserify","babel","copy"]);
};
