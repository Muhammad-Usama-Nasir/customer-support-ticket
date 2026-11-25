package com.CustomerSupportTicket.api.Database;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import com.CustomerSupportTicket.api.GenericDTOs.ORMDeleteRecord;
import com.CustomerSupportTicket.api.GenericDTOs.StoredProcedureParameters;
import com.CustomerSupportTicket.api.Utils.EnumUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DBOperations {

    private EntityManagerFactory emFactory;

    public DBOperations(EntityManagerFactory emFactory) {
        this.emFactory = emFactory;
    }

    public Object saveEntityWithIdentity(Object object) {
        EntityManager manager = emFactory.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        try {
            manager.persist(object);
            manager.flush();
            object = manager.merge(object);
            transaction.commit();
            return object;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException(" *** fnc Save Entity Error ***: " + object, e);
        } finally {
            manager.close();
        }
    }

    public int saveEntity(Object object, EnumUtil operation) {
        EntityManager manager = emFactory.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        int result = 0;
        transaction.begin();
        try {
            if (object != null) {
                if (operation.equals(EnumUtil.ADD)) {
                    manager.persist(object);
                } else {
                    manager.merge(object);
                }
                result = 1;
                transaction.commit();
            }
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException(" *** fnc Save Entity Error ***: " + object, e);
        } finally {
            manager.close();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> StoredProcedureQuery(String proc_name, Class<T> resultClass,
            List<StoredProcedureParameters> lstparams) {
        EntityManager manager = emFactory.createEntityManager();
        List<T> result = null;
        String sp = " *** Procedure: *** " + proc_name;
        try {
            StoredProcedureQuery proc = manager.createStoredProcedureQuery(proc_name, resultClass);
            if (lstparams != null && lstparams.size() > 0) {
                for (int i = 0; i < lstparams.size(); i++) {
                    StoredProcedureParameters param = lstparams.get(i);
                    proc.registerStoredProcedureParameter(param.paramName(), param.paramType(), param.parameterMode());
                    proc.setParameter(param.paramName(), param.paramValue());
                    sp += " '" + param.paramValue().toString() + "',";
                }
            }
            System.out.println(sp);
            proc.execute();
            result = proc.getResultList();
            if (result != null) {
                return result;
            } else {
                return null;
            }
        } catch (NoResultException nre) {
            System.err.println("No results returned for stored procedure: " + proc_name);
            return null;
        } catch (Exception e) {
            System.err.println("Exception occurred while executing stored procedure: " + proc_name);
            e.printStackTrace();
            throw new RuntimeException("Error executing stored procedure: " + proc_name, e);
        } finally {
            if (manager != null && manager.isOpen()) {
                manager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Object StoredProcedureQuerySingleObject(String proc_name, Class<?> resultClass,
            List<StoredProcedureParameters> lstparams) {
        EntityManager manager = emFactory.createEntityManager();
        String sp = " *** Procedure: *** " + proc_name;
        try {
            StoredProcedureQuery proc = manager.createStoredProcedureQuery(proc_name, resultClass);
            for (int i = 0; i < lstparams.size(); i++) {
                StoredProcedureParameters param = lstparams.get(i);
                proc.registerStoredProcedureParameter(param.paramName(), param.paramType(), param.parameterMode());
                proc.setParameter(param.paramName(), param.paramValue());
                sp += " '" + param.paramValue().toString() + "', ";
            }
            // System.out.println(sp);
            proc.execute();
            return proc.getSingleResult();
        } catch (NoResultException nre) {
            System.err.println("No results returned for stored procedure: " + proc_name);
            return null;
        } catch (Exception e) {
            System.err.println("Exception occurred while executing stored procedure: " + proc_name);
            e.printStackTrace();
            throw new RuntimeException("Error executing stored procedure: " + proc_name, e);
        } finally {
            if (manager != null && manager.isOpen()) {
                manager.close();
            }
        }
    }

    public <T> T storedProcedureScalar(String procName, Class<T> resultType,
            List<StoredProcedureParameters> params) {
        EntityManager manager = emFactory.createEntityManager();
        try {
            manager.getTransaction().begin();
            StoredProcedureQuery proc = manager.createStoredProcedureQuery(procName); // no resultClass
            String outName = null;
            for (StoredProcedureParameters p : params) {
                proc.registerStoredProcedureParameter(p.paramName(), p.paramType(), p.parameterMode());
                if (p.parameterMode() == ParameterMode.OUT) {
                    if (outName == null)
                        outName = p.paramName();
                } else {
                    proc.setParameter(p.paramName(), p.paramValue());
                }
            }
            Object raw;
            if (outName != null) {
                proc.execute();
                raw = proc.getOutputParameterValue(outName);
            } else {
                try {
                    raw = proc.getSingleResult();
                } catch (NoResultException e) {
                    raw = null;
                }
            }
            if (raw instanceof Object[] row) {
                raw = row[0];
            }
            manager.getTransaction().commit();
            // Convert to the requested type (SQL Server INT arrives as Integer, BIGINT as
            // Long, etc.)
            if (raw == null)
                return null;
            if (resultType.isInstance(raw))
                return resultType.cast(raw);
            if (raw instanceof Number n) {
                if (resultType == Integer.class)
                    return resultType.cast(n.intValue());
                if (resultType == Long.class)
                    return resultType.cast(n.longValue());
                if (resultType == Double.class)
                    return resultType.cast(n.doubleValue());
                if (resultType == BigDecimal.class)
                    return resultType.cast(new BigDecimal(n.toString()));
            }
            if (resultType == String.class)
                return resultType.cast(raw.toString());
            throw new IllegalStateException("Cannot convert " + raw.getClass().getName()
                    + " to " + resultType.getName() + " for procedure " + procName);

        } catch (RuntimeException e) {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            throw new RuntimeException("Error executing stored procedure: " + procName, e);
        } finally {
            if (manager.isOpen()) {
                manager.close();
            }
        }
    }

    public <T> T ExecuteQuery(String sql, Class<T> resultClass) {
        EntityManager manager = emFactory.createEntityManager();
        try {
            Query query = manager.createNativeQuery(sql, resultClass);
            Object result = query.getSingleResult();
            if (result == null) {
                return null;
            }
            return resultClass.cast(result);
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error executing query: " + e.getMessage(), e);
        } finally {
            if (manager != null && manager.isOpen()) {
                manager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public String ExecuteQueryAsString(String sql) {
        EntityManager manager = emFactory.createEntityManager();
        try {
            Query query = manager.createNativeQuery(sql);
            Object result = query.getSingleResult();
            return result != null ? result.toString() : null;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error executing query: " + e.getMessage(), e);
        } finally {
            if (manager != null && manager.isOpen()) {
                manager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Integer ExecuteUpdateStoredProcedure(String spName, List<StoredProcedureParameters> parameters) {
        EntityManager manager = emFactory.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        Integer result = 0;
        String sp = " *** Procedure: *** " + spName;
        try {
            transaction.begin();
            StoredProcedureQuery query = manager.createStoredProcedureQuery(spName);
            for (StoredProcedureParameters param : parameters) {
                query.registerStoredProcedureParameter(param.paramName(), param.paramType(), param.parameterMode());
                query.setParameter(param.paramName(), param.paramValue());
                sp += " '" + param.paramValue().toString() + "',";
            }
            System.out.println(sp);
            query.execute();
            transaction.commit();
            result = 1;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error executing stored procedure: " + spName + " — " + e.getMessage());
            throw new RuntimeException("Error executing stored procedure: " + e.getMessage(), e);
        } finally {
            if (manager != null && manager.isOpen()) {
                manager.close();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Integer ExecuteQuery(String sql) {
        EntityManager manager = emFactory.createEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        Integer result = 0;
        try {
            transaction.begin();
            Query query = manager.createNativeQuery(sql);
            query.executeUpdate();
            transaction.commit();
            result = 1;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error executing insert query: " + e.getMessage());
            throw new RuntimeException("Error executing insert query: " + e.getMessage(), e);
        } finally {
            if (manager != null && manager.isOpen()) {
                manager.close();
            }
        }
        return result;
    }

    public int deleteRecord(ORMDeleteRecord deleteRecord) {
        int result = 0;
        if (deleteRecord == null) {
            throw new IllegalArgumentException("DeleteRecord object cannot be null");
        }
        try {
            List<StoredProcedureParameters> lstParams = new ArrayList<>();
            lstParams.add(new StoredProcedureParameters("table_name", deleteRecord.getTable_name(), String.class,
                    ParameterMode.IN));
            lstParams.add(
                    new StoredProcedureParameters("column_name", deleteRecord.getColumn_name(), String.class,
                            ParameterMode.IN));
            lstParams.add(new StoredProcedureParameters("column_id", deleteRecord.getColumn_id(), String.class,
                    ParameterMode.IN));
            lstParams.add(
                    new StoredProcedureParameters("modified_user", deleteRecord.getModified_user(), String.class,
                            ParameterMode.IN));
            result = ExecuteUpdateStoredProcedure("spDeleteRecord", lstParams);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input for deleteRecord: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Exception occurred in deleteRecord: " + e.getMessage());
            throw new RuntimeException("Failed to delete record: " + e.getMessage(), e);
        }
        return result;
    }

    public String getUploadPath(String fileType) {
        String path = null;
        List<StoredProcedureParameters> lstParams = new ArrayList<>();
        lstParams.add(new StoredProcedureParameters("file_type", fileType,
                String.class, ParameterMode.IN));
        path = storedProcedureScalar("spGetUploadPath", String.class, lstParams);
        return path;
    }

}