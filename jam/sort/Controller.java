 package jam.sort;
 import jam.global.GlobalException;
 
 /** 
  * Interface that a class for data taking must implement.
  * Contains methods that SortDaemon and StorageDaemon call 
  * when done sorting and writing out data.
  *
  * @author Ken Swartz
  * @author Dale Visser
  * @version 0.9
  * @since JDK 1.1
  */
 public interface Controller {

    /**
     * Called back by <code>SortDaemon</code> thread when starting a new sort.
     * <dl>
     * <dt>OFFLINE</dt> <dd>begins a new sort</dd>
     * <dt>ONLINE</dt>  <dd>does nothing</dd>
     * </dl>
     */
    void atSortStart() throws GlobalException;   
     
    /**
     * Called back by sortDaemon thread when sort encounters a end-run-marker or a end of file marker.
     */
    void atSortEnd();     
         
    /**
     * Called by <code>SortDaemon</code> when it needs to start the next stream.
     * <dl>
     * <dt>OFFLINE</dt> <dd>gets a new input stream</dd>
     * <dt>ONLINE</dt> <dd>does nothing.</dd>
     * </dl>
     */
    boolean isSortNext();

    /**
     * Method called back by <code>StorageDaemon</code>
     * when it encouters a end-run-marker and has closed 
     * the data event file it was writing data to.
     */
    void atWriteEnd();
}
