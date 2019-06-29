# lbry-android docker-ized APK builder

This is a docker container to build lbry-android APK. In its present
configuration, you *might* be able to use this to help develop lbry-android, but
it is more focussed on building APKs in a Continuous Integration environment.

## Requirements

 * This is tested only on Linux Docker-ce 18.09.
 * The lbry-android build is very large. It exceeds the normal 10GB docker
   container limit on ext4 filesystems.
 * Docker can grow this filesystem automatically to accomodate, but only if you
   use an XFS filesystem. If you use ext4, you will just get out of space errors
   even if you have plenty of drive space on your host.
 * I didn't have an XFS filesystem on my laptop, so I can't build this locally.
 * I've tested on DigitalOcean docker droplet with 8GB ram and with an
   additional XFS volume mounted to `/var/lib/docker` (DigitalOcean root
   filesystem is still ext4.)
  * DigitalOcean is just an easy way for me to do this, but you can use any
    system configured with XFS filesystem.

## Create droplet

From your `cloud.digitalocean.com` account dashboard:

 * Create -> Droplet
 * Choose an Image -> Marketplace -> Docker
 * Choose Plan -> Standard -> $40 per month (8GB RAM)
 * Add block storage -> Add volume -> $10 per month (100GB)
  * Choose a filesystem: `XFS`
 * Choose datacenter region
 * Select your own ssh key
 * Choose a name or use default
 * Create
 
Likely you might be able to use a smaller sized droplet, this is untested.
 
## Setup docker to use XFS

SSH to the droplet as root using your ssh key.

Check the docker root:

```
$ docker info | grep Root
Docker Root Dir: /var/lib/docker
```

In this case Docker is by default using the root filesystem at `/var/lib/docker`.

Configure docker to use the volume instead:

 * Stop docker:

```
systemctl stop docker
```

 * Remove the existing docker root (this will delete all existing containers,
   images, volumes, etc.):
 
 ```
 rm /var/lib/docker/* -rf
 ```

 * Unmount the XFS volume (which should be `/dev/sda` but **double check**), and
   remount into the docker root path:
 
 ```
 umount /dev/sda
 mount /dev/sda /var/lib/docker
 ```

 * Restart docker:
 
 ```
 systemctl start docker
 ```

## Create user

To simplify the file permissions between the container and host, you should
create a user account with the same user id as inside the container. 1000 is the
default user id for the first account you create (both on the host and inside
the container.)

Create a `lbry-android` user:

```
adduser lbry-android --gecos GECOS --shell /bin/bash --disabled-password --home /home/lbry-android
gpasswd -a lbry-android docker
```

Login as the new user:

```
su lbry-android
cd /home/lbry-android
```

**You must run the rest of the commands in this documentation from the lbry-android account. Do not use root.**


## Clone lbry-android source code from github

If you don't want to clone this to `$HOME/lbry-android` just pick someplace
else, but you need to change all the references throughout this documentation as
well.

```
git clone https://github.com/lbryio/lbry-android.git $HOME/lbry-android
cd $HOME/lbry-android
```

## Configure

If you wish to use the default configuration samples that are included with the
lbry-android source, configuration is automatic. You can skip to the next
section.

If you need to customize the config, do the following:

From `$HOME/lbry-android`, create configs from default templates : 

```
cp buildozer.spec.sample buildozer.spec
cp p4a/pythonforandroid/bootstraps/lbry/build/templates/google-services.sample.json \
   p4a/pythonforandroid/bootstraps/lbry/build/templates/google-services.json
```

You can also privately fork this repository, edit the `.gitignore` file to
remove `buildozer.spec` and `google-services.json` exclusions, so that you can
permanently commit your own config files if they differ than these default
templates. If these files exist in your source tree of your forked repository,
they will be used automatically.

## Agree to the Android SDK terms and export the license

 * Download and install [android studio](https://developer.android.com/studio/) (you can do this on your laptop)
 * Go to Settings -> Android SDKs -> Select Android 6.0 (Marshmellow)
   * Click the little download button and then agree to the license."
 * Run in your terminal: 
 
 ```
 cat $HOME/Android/Sdk/licenses/android-sdk-license && echo
 ``` 
 * Copy the hexdigits output to the console - this is your Android SDK License.
 * You will need to paste your Android SDK license in the build command below.

## Build

Build the container image:

```
docker build -t lbry-android $HOME/lbry-android
```

To build the apk, you have two methods. You must pick one or the other:

 * Build from a fresh clone from github automatically.
 * Mount an already cloned directory on your host into the container at `/src`

### Build from a fresh clone from github

```
## The docker image name:
export DOCKER_IMAGE=lbry-android
## The lbry-android version/branch/commit/tag to build:
export VERSION=master
## The repository to clone from:
export REPO=https://github.com/lbryio/lbry-android.git
## The directory to store the built apk:
export LBRY_ANDROID_DIST=$HOME/lbry-android-dist
## Your provided Android SDK License string:
export ANDROID_SDK_LICENSE=xxxxxxxxxx


mkdir -p $LBRY_ANDROID_DIST
docker run --rm -it \
  -v $LBRY_ANDROID_DIST:/dist \
  -e VERSION=$VERSION \
  -e ANDROID_SDK_LICENSE=$ANDROID_SDK_LICENSE \
  $DOCKER_IMAGE
```

Using `LBRY_ANDROID_DIST` specified like above, the final apk will reside in
`$HOME/lbry-android-dist`.

### Build from an existing lbry-android directory 

```
## The directory containing lbry-android source code:
export LBRY_ANDROID_SRC=$HOME/lbry-android
## The docker image name:
export DOCKER_IMAGE=lbry-android
## Your provided Android SDK License string:
export ANDROID_SDK_LICENSE=xxxxxxxxxx

docker run --rm -it \
  -v $LBRY_ANDROID_SRC:/src \
  -e VERSION=$VERSION \
  -e ANDROID_SDK_LICENSE=$ANDROID_SDK_LICENSE \
  $DOCKER_IMAGE
```

When compiling from an existing directory, the final apk will reside in the
`./bin` subdirectory.

