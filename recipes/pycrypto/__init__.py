
from pythonforandroid.toolchain import (
    CompiledComponentsPythonRecipe,
    Recipe,
    current_directory,
    info,
    shprint,
)
from os.path import join
import sh


class PyCryptoRecipe(CompiledComponentsPythonRecipe):
    version = '2.6.1'
    url = 'https://pypi.python.org/packages/source/p/pycrypto/pycrypto-{version}.tar.gz'
    depends = ['libgmp', 'openssl', 'python2']
    site_packages_name = 'Crypto'
    call_hostpython_via_targetpython = False

    patches = ['add_length.patch']

    def get_recipe_env(self, arch=None):
        env = super(PyCryptoRecipe, self).get_recipe_env(arch)
        openssl_build_dir = Recipe.get_recipe('openssl', self.ctx).get_build_dir(arch.arch)
        target_python = Recipe.get_recipe('python2', self.ctx).get_build_dir(arch.arch)
        # include libgmp build dir for gmp.h
        libgmp_build_dir = Recipe.get_recipe('libgmp', self.ctx).get_build_dir(arch.arch)

        # set to prevent including hostpython headers to avoid
        # LONG_BIT definition appears wrong for platform error when compiling for Androidd
        env['PYTHONXCPREFIX'] = target_python
        env['LDSHARED'] = env['CC'] + ' -pthread -shared -Wl,-O1 -Wl,-Bsymbolic-functions'
        env['PYTHON_ROOT'] = join(target_python, 'python-install')
        env['CFLAGS'] += ' -I' + env['PYTHON_ROOT'] + '/include/python2.7'
        env['LDFLAGS'] += ' -L' + env['PYTHON_ROOT'] + '/lib' + ' -lpython2.7'

        env['CFLAGS'] += ' -I%s' % (join(libgmp_build_dir, 'include'))
        env['CC'] = '%s -I%s' % (env['CC'], join(openssl_build_dir, 'include'))
        env['LDFLAGS'] = env['LDFLAGS'] + ' -L{}'.format(
            self.ctx.get_libs_dir(arch.arch) +
            '-L{}'.format(self.ctx.libs_dir)) + ' -L{}'.format(
            openssl_build_dir)
        #env['EXTRA_CFLAGS'] = '--host linux-armv'
        env['ac_cv_func_malloc_0_nonnull'] = 'yes'
        return env

    def build_compiled_components(self, arch):
        info('Configuring compiled components in {}'.format(self.name))

        env = self.get_recipe_env(arch)
        with current_directory(self.get_build_dir(arch.arch)):
            configure = sh.Command('./configure')
            shprint(configure, '--host=arm-linux',
                    '--prefix={}'.format(self.ctx.get_python_install_dir()),
                    '--enable-shared', _env=env)
        super(PyCryptoRecipe, self).build_compiled_components(arch)

recipe = PyCryptoRecipe()
