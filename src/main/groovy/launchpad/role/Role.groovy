package launchpad.role

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
class Role {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @NotNull
    String name

    @NotNull
    String authority

    @OneToMany(fetch = FetchType.LAZY, mappedBy = 'role')
    Set<RolePermission> rolePermissions
}
