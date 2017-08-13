from pythonforandroid.recipe import PythonRecipe


class Pbkdf2Recipe(PythonRecipe):

    # TODO: version
    url = 'https://github.com/dlitz/python-pbkdf2/archive/master.zip'

    depends = ['setuptools']

    call_hostpython_via_targetpython = False

recipe = Pbkdf2Recipe()
