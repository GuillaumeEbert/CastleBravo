package API_Com.CommunicationManager;

import java.util.Vector;

import API_Com.CommunicationManager.CommunicationManager.ModuleType;
import API_Com.CommunicationManager.CommunicationManager.ModuleState;

/**
 * Interface representing the three event raised by the different module
 */
public interface CommunicationListener {

    interface ComManagerListener {

        /**
         * Raised when a connection is establish or failed
         *
         * @param moduleType  type of the module
         * @param id          The id of the module which has trigger the event
         * @param moduleState sate of the module
         * @param reason      The reason of the failed connection is null when the connection is successful
         * @see CommunicationManager.ModuleType
         * @see CommunicationManager.ModuleState
         */
        void onConnection(ModuleType moduleType, int id, ModuleState moduleState, String reason);

        /**
         * Raised when a module is disconnected
         * @param moduleType type of the module
         *                   @see CommunicationManager.ModuleType
         * @param id The id of the module which has trigger the event
         * @param moduleState sate of the module
         *                    @see CommunicationManager.ModuleState
         * @param e If the connection failed e contain the exception that occurs. Null if successful
         */
        void onDisconnection(ModuleType moduleType, int id, ModuleState moduleState, Exception e);

        /**
         * Raised when data are received by a module
         * @param moduleType type of the module
         *                   @see CommunicationManager.ModuleType
         * @param id  The id of the module which has trigger the event
         * @param moduleState sate of the module
         *                    @see CommunicationManager.ModuleState
         * @param dataBuffer Vector of integer containing the data
         * @param e If a error occurs during the reception e isn't null. If no errors e = null
         */
        void onDataReceived(ModuleType moduleType, int id, ModuleState moduleState, Vector<Integer> dataBuffer, Exception e);
    }
}
