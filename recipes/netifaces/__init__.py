
import glob
from pythonforandroid.toolchain import CompiledComponentsPythonRecipe, Recipe
from os.path import join
import sh


class NetifacesRecipe(CompiledComponentsPythonRecipe):
    version = '0.10.7'
    url = 'https://files.pythonhosted.org/packages/81/39/4e9a026265ba944ddf1fea176dbb29e0fe50c43717ba4fcf3646d099fe38/netifaces-{version}.tar.gz'
    depends = ['python2', 'setuptools']
    call_hostpython_via_targetpython = False

    def get_recipe_env(self, arch):
        env = super(NetifacesRecipe, self).get_recipe_env(arch)

        target_python = Recipe.get_recipe('python2', self.ctx).get_build_dir(arch.arch)
        env['PYTHON_ROOT'] = join(target_python, 'python-install')
        env['LDSHARED'] = env['CC'] + ' -pthread -shared -Wl,-O1 -Wl,-Bsymbolic-functions'
        env['CFLAGS'] += ' -I' + env['PYTHON_ROOT'] + '/include/python2.7'
        env['LDFLAGS'] += ' -L' + env['PYTHON_ROOT'] + '/lib' + ' -lpython2.7'

        return env

recipe = NetifacesRecipe()
