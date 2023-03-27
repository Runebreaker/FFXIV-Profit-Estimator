import SwiftUI
import shared

struct ContentView: View {
	let greet = App().greet()

	var body: some View {
		Text(greet)
            .font(.title)
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
