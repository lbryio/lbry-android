from kivy.app import App
from kivy.lang import Builder
from kivy.utils import platform

kv = '''
Button:
    text: 'push me!'
'''

class ServiceApp(App):
    def build(self):
        if platform == 'android':
            from jnius import autoclass

            Intent = autoclass('android.content.Intent')
            LbrynetService = autoclass('io.lbry.lbrynet.LbrynetService')
            context = autoclass('org.kivy.android.PythonActivity').mActivity

            LbrynetService.start(context, '')

            # close the activity once the service starts
            # ideally, we should have some form of service control for the activity
            context.finish()

        return Builder.load_string(kv)

if __name__ == '__main__':
    ServiceApp().run()
