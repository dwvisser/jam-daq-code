package jam.commands;

/**
 * Given an object, return true or false.
 * @author Dale Visser
 * @param <T>
 *            the argument type to evaluate for
 */
interface Predicate<T> {

    /**
     * Given an object, return true or false.
     * @param object
     *            to evaluate based on
     * @return true or false
     */
    boolean evaluate(final T object);
}
