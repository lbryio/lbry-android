import glob
from pythonforandroid.toolchain import (
    CythonRecipe,
    Recipe,
    current_directory,
    info,
    shprint,
)
from os.path import join
import sh


class UnqliteRecipe(CythonRecipe):
    version = '0.6.0'
    url = 'https://pypi.python.org/packages/cb/4e/e1f64a3d0f6462167805940b4c72f47bafc1129e363fc4c0f79a1cdc5dd1/unqlite-{version}.tar.gz'
    depends = [('python2', 'python3crystax'), 'setuptools']
    call_hostpython_via_targetpython = False

    patches = ['setup.patch']

    def get_recipe_env(self, arch):
        env = super(UnqliteRecipe, self).get_recipe_env(arch)

        target_python = Recipe.get_recipe('python3crystax', self.ctx).get_build_dir(arch.arch)
        env['PYTHON_ROOT'] = join(target_python, 'python-install')
        env['CFLAGS'] += ' -I' + env['PYTHON_ROOT'] + '/include/python3.6'
        env['LDFLAGS'] += ' -L' + env['PYTHON_ROOT'] + '/lib' + ' -lpython3.6m'

        return env


recipe = UnqliteRecipe()
