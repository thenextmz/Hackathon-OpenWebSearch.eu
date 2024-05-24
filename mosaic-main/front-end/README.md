# Front-end

This front-end application is part of the Prototype Search Application and demonstrates a basic approach to use the search service. You can either put this front-end application into a directory of your local web server or you can run the following commands to build a Docker image and run the Docker container in this directory to access the search service:
```shell
# create an image
docker build -t front-end .
# start the container
docker run -p 80:80 front-end
```

If you use the port 80 to run the container, you should be able to access the front-end at http://localhost.
