import sys
import StringIO

from twisted.trial.runner import (
    TestLoader,
    TrialRunner
)
from twisted.trial.reporter import TreeReporter
from twisted.plugin import getPlugins, IPlugin
from jnius import autoclass
import lbrynet.tests
from os import listdir

str_stream = StringIO.StringIO()

serviceClass = autoclass('io.lbry.lbrynet.LbrynetTestRunnerService')

def update_output_in_activity(str):
    service = serviceClass.serviceInstance
    if service is not None:
        service.broadcastTestRunnerOutput(str)

class AndroidTestReporter(TreeReporter):
    def addSuccess(self, test):
        super(TreeReporter, self).addSuccess(test)
        self.endLine('[OK]', self.SUCCESS)
        update_output_in_activity(str_to_basic_html(self._stream.getvalue()))
        
    def addError(self, *args):
        super(TreeReporter, self).addError(*args)
        self.endLine('[ERROR]', self.ERROR)
        update_output_in_activity(str_to_basic_html(self._stream.getvalue()))

    def addFailure(self, *args):
        super(TreeReporter, self).addFailure(*args)
        self.endLine('[FAIL]', self.FAILURE)
        update_output_in_activity(str_to_basic_html(self._stream.getvalue()))
        
    def addSkip(self, *args):
        super(TreeReporter, self).addSkip(*args)
        self.endLine('[SKIPPED]', self.SKIP)
        update_output_in_activity(str_to_basic_html(self._stream.getvalue()))
        
    def addExpectedFailure(self, *args):
        super(TreeReporter, self).addExpectedFailure(*args)
        self.endLine('[TODO]', self.TODO)
        update_output_in_activity(str_to_basic_html(self._stream.getvalue()))
        
    def addUnexpectedSuccess(self, *args):
        super(TreeReporter, self).addUnexpectedSuccess(*args)
        self.endLine('[SUCCESS!?!]', self.TODONE)
        update_output_in_activity(str_to_basic_html(self._stream.getvalue()))

    def startTest(self, test):
        super(AndroidTestReporter, self).startTest(test)
        update_output_in_activity(str_to_basic_html(self._stream.getvalue()))

    def endLine(self, message, color):
        super(AndroidTestReporter, self).endLine(message, color)
        update_output_in_activity(str_to_basic_html(self._stream.getvalue()))

def str_to_basic_html(value):
    return value.replace("\n", "<br>").replace(" ", '&nbsp;')

def _parseLocalVariables(line):
    """
    Accepts a single line in Emacs local variable declaration format and
    returns a dict of all the variables {name: value}.
    Raises ValueError if 'line' is in the wrong format.
    See http://www.gnu.org/software/emacs/manual/html_node/File-Variables.html
    """
    paren = '-*-'
    start = line.find(paren) + len(paren)
    end = line.rfind(paren)
    if start == -1 or end == -1:
        raise ValueError("%r not a valid local variable declaration" % (line,))
    items = line[start:end].split(';')
    localVars = {}
    for item in items:
        if len(item.strip()) == 0:
            continue
        split = item.split(':')
        if len(split) != 2:
            raise ValueError("%r contains invalid declaration %r"
                             % (line, item))
        localVars[split[0].strip()] = split[1].strip()
    return localVars

def isTestFile(filename):
    """
    Returns true if 'filename' looks like a file containing unit tests.
    False otherwise.  Doesn't care whether filename exists.
    """
    basename = os.path.basename(filename)
    return (basename.startswith('test_')
            and os.path.splitext(basename)[1] == ('.py'))

def loadLocalVariables(filename):
    """
    Accepts a filename and attempts to load the Emacs variable declarations
    from that file, simulating what Emacs does.
    See http://www.gnu.org/software/emacs/manual/html_node/File-Variables.html
    """
    with open(filename, "r") as f:
        lines = [f.readline(), f.readline()]
    for line in lines:
        try:
            return _parseLocalVariables(line)
        except ValueError:
            pass
    return {}

def getTestModules(filename):
    testCaseVar = loadLocalVariables(filename).get('test-case-name', None)
    if testCaseVar is None:
        return []
    return testCaseVar.split(',')

def run():
    loader = TestLoader();
    suite = loader.loadPackage(lbrynet.tests, True)
    
    
    filename = '/data/user/0/io.lbry.lbrynet/files/app/lib/python2.7/site-packages/lbrynet/tests'
    filelist = listdir(filename)
    print filelist
    '''
    tests = []
    if not os.path.isfile(filename):
        sys.stderr.write("File %r doesn't exist\n" % (filename,))
        return
        
    filename = os.path.abspath(filename)
    if isTestFile(filename):
        tests.append(filename)
    else:
        tests.extend(getTestModules(filename))
    
    suite = loader.loadByNames(tests, True)
    '''
    runner = TrialRunner(AndroidTestReporter)
    runner.stream = str_stream
    passFail = not runner.run(suite).wasSuccessful()

    print str_stream.getvalue()
    sys.exit(passFail)

if __name__ == '__main__':
    run()
