__version__ = "0.8.4"

class ServiceApp(App):
    def build(self):
        from jnius import autoclass

        Intent = autoclass('android.content.Intent')
        LbrynetService = autoclass('io.lbry.browser.LbrynetService')
        
if __name__ == '__main__':
    ServiceApp().run()

