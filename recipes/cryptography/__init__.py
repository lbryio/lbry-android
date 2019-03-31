from pythonforandroid.recipe import CompiledComponentsPythonRecipe, Recipe
from os.path import dirname, join
import os

class CryptographyRecipe(CompiledComponentsPythonRecipe):
    name = 'cryptography'
    version = '2.5'
    url = 'https://github.com/pyca/cryptography/archive/{version}.tar.gz'
    depends = [('python2', 'python3crystax'), 'openssl', 'idna', 'pyasn1', 'six', 'setuptools', 'ipaddress', 'cffi']
    call_hostpython_via_targetpython = False

    def get_recipe_env(self, arch):
        env = super(CryptographyRecipe, self).get_recipe_env(arch)
        r = self.get_recipe('openssl', self.ctx)
        openssl_dir = r.get_build_dir(arch.arch)
        env['CFLAGS'] += ' -I' + join(openssl_dir, 'include') + ' -w'
        # Set linker to use the correct gcc
        env['LDSHARED'] = env['CC'] + ' -pthread -shared -Wl,-O1 -Wl,-Bsymbolic-functions'
        env['LDFLAGS'] += ' -L' + openssl_dir + \
                          ' -lssl' + r.version + \
                          ' -lcrypto' + r.version

        if self.ctx.ndk == 'crystax':
            # only keeps major.minor (discards patch)
            python_version = self.ctx.python_recipe.version[0:3]
            ndk_dir_python = os.path.join(self.ctx.ndk_dir, 'sources/python/', python_version)
            env['LDFLAGS'] += ' -L{}'.format(os.path.join(ndk_dir_python, 'libs', arch.arch))
            env['LDFLAGS'] += ' -lpython{}m'.format(python_version)
            # until `pythonforandroid/archs.py` gets merged upstream:
            # https://github.com/kivy/python-for-android/pull/1250/files#diff-569e13021e33ced8b54385f55b49cbe6
            env['CFLAGS'] += ' -I{}/include/python/'.format(ndk_dir_python) + ' -L{}'.format(os.path.join(ndk_dir_python, 'libs', arch.arch))


        return env

recipe = CryptographyRecipe()
