
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

    call_hostpython_via_targetpython = False

recipe = UnqliteRecipe()
