
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


class TwistedRecipe(CythonRecipe):
    version = '18.7.0'
    url = 'https://github.com/twisted/twisted/archive/twisted-{version}.tar.gz'

    depends = ['setuptools', 'zope_interface', 'incremental', 'constantly']

    call_hostpython_via_targetpython = False
    install_in_hostpython = True

    def prebuild_arch(self, arch):
        super(TwistedRecipe, self).prebuild_arch(arch)
        # TODO Need to whitelist tty.pyo and termios.so here
        print('Should remove twisted tests etc. here, but skipping for now')

    def get_recipe_env(self, arch):
        env = super(TwistedRecipe, self).get_recipe_env(arch)

        # TODO: Move this and others to base Recipe class for Cython and CompiledComponent recipes
        target_python = Recipe.get_recipe('python3crystax', self.ctx).get_build_dir(arch.arch)
        env['PYTHON_ROOT'] = join(target_python, 'python-install')
        env['CFLAGS'] += ' -I' + env['PYTHON_ROOT'] + '/include/python3.6'
        env['LDFLAGS'] += ' -L' + env['PYTHON_ROOT'] + '/lib' + ' -lpython3.6m'

        # We add BUILDLIB_PATH to PYTHONPATH so twisted can find _io.so
        env['PYTHONPATH'] = ':'.join([
            self.ctx.get_site_packages_dir(),
            env['BUILDLIB_PATH'],
        ])
        return env

recipe = TwistedRecipe()
