from pythonforandroid.recipe import CompiledComponentsPythonRecipe, Recipe
from os.path import join


class CffiRecipe(CompiledComponentsPythonRecipe):
	name = 'cffi'
	version = '1.11.0'
	#url = 'https://pypi.python.org/packages/source/c/cffi/cffi-{version}.tar.gz'
	url = 'https://pypi.python.org/packages/4e/32/4070bdf32812c89eb635c80880a5caa2e0189aa7999994c265577e5154f3/cffi-{version}.tar.gz'

	depends = [('python2', 'python3crystax'), 'setuptools', 'pycparser', 'libffi']

	patches = ['disable-pkg-config.patch']

	# call_hostpython_via_targetpython = False
	install_in_hostpython = True

	def get_recipe_env(self, arch=None):
		env = super(CffiRecipe, self).get_recipe_env(arch)
		libffi = self.get_recipe('libffi', self.ctx)
		includes = libffi.get_include_dirs(arch)
		env['CFLAGS'] = ' -I'.join([env.get('CFLAGS', '')] + includes)
		env['LDFLAGS'] = (env.get('CFLAGS', '') + ' -L' +
		                  self.ctx.get_libs_dir(arch.arch))
		env['LDSHARED'] = env['CC'] + ' -pthread -shared -Wl,-O1 -Wl,-Bsymbolic-functions'
		env['PYTHONPATH'] = ':'.join([self.ctx.get_site_packages_dir(), env['BUILDLIB_PATH']])
		
		target_python = Recipe.get_recipe('python2', self.ctx).get_build_dir(arch.arch)
		env['PYTHON_ROOT'] = join(target_python, 'python-install')
		env['CFLAGS'] += ' -I' + env['PYTHON_ROOT'] + '/include/python2.7'
		env['LDFLAGS'] += ' -L' + env['PYTHON_ROOT'] + '/lib' + ' -lpython2.7'
		
		return env


recipe = CffiRecipe()
