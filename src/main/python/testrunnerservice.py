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

serviceClass = autoclass('io.lbry.browser.LbrynetTestRunnerService')

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

def run():
    loader = TestLoader();
    suite = loader.loadPackage(lbrynet.tests, True)
    runner = TrialRunner(AndroidTestReporter)
    runner.stream = str_stream
    passFail = not runner.run(suite).wasSuccessful()

    print str_stream.getvalue()
    sys.exit(passFail)

if __name__ == '__main__':
    run()
