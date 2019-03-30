from functools import partial

from pythonforandroid.recipe import Recipe
from pythonforandroid.toolchain import shprint, current_directory
from os.path import join
import sh


class OpenSSLRecipe(Recipe):
    version = '1.1'
    url_version = '1.1.1b'
    url = 'https://www.openssl.org/source/openssl-{url_version}.tar.gz'

    @property
    def versioned_url(self):
        if self.url is None:
            return None
        return self.url.format(url_version=self.url_version)

    def should_build(self, arch):
        return not self.has_libs(arch, 'libssl' + self.version + '.so',
                                 'libcrypto' + self.version + '.so')

    def check_symbol(self, env, sofile, symbol):
        nm = env.get('NM', 'nm')
        syms = sh.sh('-c', "{} -gp {} | cut -d' ' -f3".format(
                nm, sofile), _env=env).splitlines()
        if symbol in syms:
            return True
        print('{} missing symbol {}; rebuilding'.format(sofile, symbol))
        return False

    def get_recipe_env(self, arch=None):
        env = super(OpenSSLRecipe, self).get_recipe_env(arch)
        env['OPENSSL_VERSION'] = self.version
        env['MAKE'] = 'make' # This removes the '-j5', which isn't safe
        env['CFLAGS'] += ' ' + env['LDFLAGS']
        env['CC'] += ' ' + env['LDFLAGS']
        env['ANDROID_NDK'] = self.ctx.ndk_dir
        return env

    def select_build_arch(self, arch):
        aname = arch.arch
        if 'arm64' in aname:
            return 'android-arm64'
        if 'v7a' in aname:
            return 'android-arm'
        if 'arm' in aname:
            return 'android'
        return 'linux-armv4'

    def build_arch(self, arch):
        env = self.get_recipe_env(arch)
        with current_directory(self.get_build_dir(arch.arch)):
            # sh fails with code 255 trying to execute ./Configure
            # so instead we manually run perl passing in Configure
            perl = sh.Command('perl')
            buildarch = self.select_build_arch(arch)

            config_args = ['shared', 'no-dso', 'no-asm']
            config_args.append(buildarch)
            shprint(perl, 'Configure', *config_args, _env=env)
            self.apply_patch('disable-sover.patch', arch.arch)

            makefile = join(self.get_build_dir(arch.arch), 'Makefile')
            sh.sed('-i', 's/CROSS_COMPILE=arm-linux-androideabi-/CROSS_COMPILE=/g', makefile)
            shprint(sh.make, 'build_libs', _env=env)

            self.install_libs(arch, 'libssl.a', 'libssl' + self.version + '.so',
                              'libcrypto.a', 'libcrypto' + self.version + '.so')

recipe = OpenSSLRecipe()
