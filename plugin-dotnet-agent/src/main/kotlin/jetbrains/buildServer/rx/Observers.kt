package jetbrains.buildServer.rx

inline fun <T> observer(crossinline onNext: (T) -> Unit): Observer<T> = observer(onNext, {}, {})

inline fun <T> observer(crossinline onNext: (T) -> Unit, crossinline onError: (Exception) -> Unit, crossinline onComplete: () -> Unit): Observer<T> =
        object : Observer<T> {
            override fun onNext(value: T) = onNext(value)
            override fun onError(error: Exception) = onError(error)
            override fun onComplete() = onComplete()
        }

fun <T> emptyObserver(): Observer<T> = object : Observer<T> {
    override fun onNext(value: T) = Unit
    override fun onError(error: Exception) = Unit
    override fun onComplete() = Unit
}
