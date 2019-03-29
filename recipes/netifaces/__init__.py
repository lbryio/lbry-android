
import glob
from pythonforandroid.recipe import CompiledComponentsPythonRecipe, Recipe
from os.path import join
import os
import sh


class NetifacesRecipe(CompiledComponentsPythonRecipe):
    version = '0.10.7'
    url = 'https://files.pythonhosted.org/packages/81/39/4e9a026265ba944ddf1fea176dbb29e0fe50c43717ba4fcf3646d099fe38/netifaces-{version}.tar.gz'
    depends = [('python2', 'python3crystax'), 'setuptools']
    call_hostpython_via_targetpython = False
    patches = ['socket-ioctls.patch']

    def get_recipe_env(self, arch):
        env = super(NetifacesRecipe, self).get_recipe_env(arch)

        env['LDSHARED'] = env['CC'] + ' -pthread -shared -Wl,-O1 -Wl,-Bsymbolic-functions'

        if self.ctx.ndk == 'crystax':
            # only keeps major.minor (discards patch)
            python_version = self.ctx.python_recipe.version[0:3]
            ndk_dir_python = os.path.join(self.ctx.ndk_dir, 'sources/python/', python_version)
            env['LDFLAGS'] += ' -L{}'.format(os.path.join(ndk_dir_python, 'libs', arch.arch))
            env['LDFLAGS'] += ' -lpython{}m'.format(python_version)
            # until `pythonforandroid/archs.py` gets merged upstream:
            # https://github.com/kivy/python-for-android/pull/1250/files#diff-569e13021e33ced8b54385f55b49cbe6
            env['CFLAGS'] += ' -I{}/include/python/'.format(ndk_dir_python)

        return env

recipe = NetifacesRecipe()
