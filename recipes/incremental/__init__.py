
from pythonforandroid.toolchain import PythonRecipe, shprint
import sh


class IncrementalRecipe(PythonRecipe):
    version = '17.5.0'
    url = 'https://pypi.python.org/packages/8f/26/02c4016aa95f45479eea37c90c34f8fab6775732ae62587a874b619ca097/incremental-{version}.tar.gz'

    depends = [('python2', 'python3crystax'), 'setuptools']

    call_hostpython_via_targetpython = False
    install_in_hostpython = True

recipe = IncrementalRecipe()
