package domain

import state.State
// TODO why do we require StateObject and State ? Was I drunk ?
abstract class StateObject(var value: State):Cloneable