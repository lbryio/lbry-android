from pythonforandroid.toolchain import Recipe, shprint, current_directory, ArchARM
from os.path import exists, join, realpath
from os import uname
import glob
import sh


class LibGMPRecipe(Recipe):
    version = '6.1.2'
    url = 'https://gmplib.org/download/gmp/gmp-{version}.tar.bz2'

    def should_build(self, arch):
        build_dir = self.get_build_dir(arch.arch)
        return not exists(join(build_dir, '.libs', 'libgmp.so'))

    def get_recipe_env(self, arch=None):
        env = super(LibGMPRecipe, self).get_recipe_env(arch)
        env['LIBGMP_LDFLAGS'] = '-avoid-version'

        return env


    def build_arch(self, arch):
        with current_directory(self.get_build_dir(arch.arch)):
            env = self.get_recipe_env(arch)
            configure = sh.Command('./configure')
            shprint(configure,
                    '--host=arm-linux',
                    _env=env)
            shprint(sh.make, '-j4', _env=env)
            shprint(sh.mkdir, 'include')
            shprint(sh.cp, '-a', 'gmp.h', 'include/gmp.h')
            shprint(sh.cp, '-a', '.libs/libgmp.so', join(self.ctx.get_libs_dir(arch.arch), 'libgmp.so'))
            shprint(sh.cp, '-a', '.libs/libgmp.so', join(self.ctx.get_libs_dir(''), 'libgmp.so')) # also copy to libs_collections/<package_name>

recipe = LibGMPRecipe()
