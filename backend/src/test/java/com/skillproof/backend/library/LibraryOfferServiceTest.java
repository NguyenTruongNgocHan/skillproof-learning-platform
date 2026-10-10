package com.skillproof.backend.library;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.skillproof.backend.library.application.LibraryOfferService;
import com.skillproof.backend.library.infrastructure.persistence.*;
import com.skillproof.backend.organization.contract.OrganizationPublicQuery;
class LibraryOfferServiceTest {
    private final LibraryResourceRepository resources=mock(LibraryResourceRepository.class);
    private final OrganizationPublicQuery organizations=mock(OrganizationPublicQuery.class);
    private final LibraryOfferService offers=new LibraryOfferService(resources,organizations);
    private LibraryResourceEntity resource(UUID organization) {
        var value=new LibraryResourceEntity(UUID.randomUUID(),UUID.randomUUID(),organization,"Resource","Summary","Text","PUBLISHED",0,Instant.now(),null,null);
        when(resources.findById(value.getId())).thenReturn(Optional.of(value));return value;
    }
    @Test void personalFreeResourceIsPublic() {
        var r=resource(null);assertTrue(offers.offer(r.getId()).available());assertTrue(offers.offer(r.getId()).purchasable());
    }
    @Test void restrictedOrganizationResourceRequiresGrantInsteadOfPayment() {
        UUID organization=UUID.randomUUID();var r=resource(organization);r.configureAccess("RESTRICTED");
        when(organizations.find(organization)).thenReturn(Optional.of(new OrganizationPublicQuery.OrganizationView(organization,"Organization","APPROVED")));
        assertTrue(offers.offer(r.getId()).available());assertFalse(offers.offer(r.getId()).purchasable());
    }
    @Test void lossOfOrganizationApprovalBlocksResourceAvailability() {
        UUID organization=UUID.randomUUID();var r=resource(organization);
        when(organizations.find(organization)).thenReturn(Optional.of(new OrganizationPublicQuery.OrganizationView(organization,"Organization","REJECTED")));
        assertFalse(offers.offer(r.getId()).available());
    }
}
