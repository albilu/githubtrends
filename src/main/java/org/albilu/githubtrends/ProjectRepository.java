package org.albilu.githubtrends;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class ProjectRepository {

    // Create
    public void saveProject(Project project) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(project);  // Hibernate handles the insert statement
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    // Read
    public Project getUser(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Project.class, id);  // Hibernate handles the select statement
        }
    }

    // Method to check if a user exists by name
    public boolean existsByName(String projectname) {
        Transaction transaction = null;
        boolean exists = false;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Create a query to check if a user with the given name exists
            String hql = "SELECT COUNT(u) FROM Project u WHERE u.projectName= :projectName";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("projectName", projectname);

            Long count = query.uniqueResult();  // This will return the count of users with the given name

            exists = (count != null && count > 0);  // If count > 0, the user exists

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        return exists;
    }

    // Method to retrieve all users
    public List<Project> findAllUsersSortByStarsAndTrendStars() {
        Transaction transaction = null;
        List<Project> projects = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Create a query to retrieve all users
            String hql = "FROM Project u WHERE u.isNew = true ORDER BY u.trendStars DESC, u.stars DESC";
            Query<Project> query = session.createQuery(hql, Project.class);
            projects = query.getResultList();  // Retrieve the list of users

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        return projects;
    }

    // Update
    public void updateUser(Project project) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(project);  // Hibernate handles the update statement
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    // Delete
    public void deleteUser(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Project project = session.get(Project.class, id);
            if (project != null) {
                session.delete(project);  // Hibernate handles the delete statement
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
