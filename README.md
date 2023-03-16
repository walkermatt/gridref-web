# GridRef Web

A super simple service to convert an alpha numeric Ordnance Survey grid reference to easting / northing or easting / northing to a grid reference.

View online at [https://gridref.longwayaround.org.uk](https://gridref.longwayaround.org.uk).

## Usage

To start a local web server for development you can use ring:

```sh
lein ring server-headless
```

## Docker

Build and run a Docker image based on the `Dockerfile`.

```sh
docker build -t gridref-web .
docker run -it --rm --memory-swap 250m --memory 250m --env PORT=8181 -p 8181:8181 --name gridref-web gridref-web
```

### Notes

Specifying the same value for `--memory-swap` and `--memory` effectively limits
the container to that much memory without swap which mimics the fly.io
free-tier.

The above sets the `PORT` environment variable which is used in the Dockerfile
to determine which port the embedded app server listens on; the `-p` option
exposes the container port to localhost for testing.

## Fly.io

Initial setup and deployment under a fly.dev domain:

```sh
flyctl auth login
fly launch
```

Choices made via `fly launch`:

```sh
# ? Choose an app name (leave blank to generate one): gridref-web
# ? Choose a region for deployment: London, United Kingdom (lhr)
# Created app 'gridref-web' in organization 'personal'
# ? Would you like to set up a Postgresql database now? No
# ? Would you like to set up an Upstash Redis database now? No
# ? Create .dockerignore from 3 .gitignore files? Yes
# ? Would you like to deploy now? No
```

Check the generated `fly.toml` (no changes needed) so we can deploy which will
make the app available under a fly.dev sub-domain.

```sh
flyctl deploy
```

Setup a sub-domain and cert:

1. Create a CNAME to pointing at the fly.dev sub-domain of the app
2. Request a certificate via the `flyctl`:

```sh
flyctl certs create gridref.longwayaround.org.uk
```

App is available at
[https://gridref.longwayaround.org.uk/](https://gridref.longwayaround.org.uk/).
