import SwiftUI
import shared

struct ContentView: View {
	let greet = App().greet()

	var body: some View {
        List(0 ..< 20) { item in
            Text(greet)
                .font(.largeTitle)
        }
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
