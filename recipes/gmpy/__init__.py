
import glob
from pythonforandroid.recipe import CythonRecipe, Recipe
from pythonforandroid.toolchain import (
    current_directory,
    info,
    shprint,
)
from os.path import join
import sh


class GmpyRecipe(CythonRecipe):
    version = '1.17'
    url = 'https://pypi.python.org/packages/26/37/2184c13cee81e1dbeaebbb13570195247e73ab2138a3db0c9d2c5347e372/gmpy-{version}.zip'

    depends = ['libgmp', 'setuptools']

    call_hostpython_via_targetpython = False
    install_in_hostpython = True

    def get_recipe_env(self, arch):
        env = super(GmpyRecipe, self).get_recipe_env(arch)

        # include libgmp build dir for gmp.h
        libgmp_build_dir = Recipe.get_recipe('libgmp', self.ctx).get_build_dir(arch.arch)
        env['CFLAGS'] += ' -I%s' % (join(libgmp_build_dir, 'include'))

        target_python = Recipe.get_recipe('python3crystax', self.ctx).get_build_dir(arch.arch)
        env['PYTHON_ROOT'] = join(target_python, 'python-install')
        env['CFLAGS'] += ' -I' + env['PYTHON_ROOT'] + '/include/python3.7'
        env['LDFLAGS'] += ' -L' + env['PYTHON_ROOT'] + '/lib' + ' -lpython3.7m'

        return env

recipe = GmpyRecipe()
