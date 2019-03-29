
import glob
from pythonforandroid.recipe import CythonRecipe, Recipe
from pythonforandroid.toolchain import (
    current_directory,
    info,
    shprint,
)
from os.path import join
import sh


class MiniupnpcRecipe(CythonRecipe):
    version = '1.9'
    url = 'https://pypi.python.org/packages/55/90/e987e28ed29b571f315afea7d317b6bf4a551e37386b344190cffec60e72/miniupnpc-{version}.tar.gz'
    depends = ['setuptools']

    call_hostpython_via_targetpython = False
    install_in_hostpython = True

    def get_recipe_env(self, arch):
        env = super(MiniupnpcRecipe, self).get_recipe_env(arch)

        target_python = Recipe.get_recipe('python3crystax', self.ctx).get_build_dir(arch.arch)
        env['PYTHON_ROOT'] = join(target_python, 'python-install')
        env['CFLAGS'] += ' -I' + env['PYTHON_ROOT'] + '/include/python3.7'
        env['LDFLAGS'] += ' -L' + env['PYTHON_ROOT'] + '/lib' + ' -lpython3.7m'

        return env

recipe = MiniupnpcRecipe()
